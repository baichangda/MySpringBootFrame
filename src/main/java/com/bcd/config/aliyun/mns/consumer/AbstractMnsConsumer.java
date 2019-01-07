package com.bcd.config.aliyun.mns.consumer;

import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.model.Message;
import com.aliyun.mns.model.QueueMeta;
import com.aliyun.mns.model.SubscriptionMeta;
import com.aliyun.mns.model.TopicMeta;
import com.bcd.config.aliyun.properties.mns.MnsTopicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public abstract class AbstractMnsConsumer implements Runnable{
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private static final int BATCH_NUM = 10;

    private CloudQueue queue;

    private int queueWaitSeconds;

    private boolean enable;

    public AbstractMnsConsumer(MNSClient mnsClient, MnsTopicProperties mnsTopicProperties) {
        this.enable=mnsTopicProperties.enable;
        if(enable){
            this.queueWaitSeconds =mnsTopicProperties.queueWaitSeconds;
            initMns(mnsClient, mnsTopicProperties.topic, mnsTopicProperties.queue, mnsTopicProperties.subscription);
        }
    }

    public void consume() {
        if(!enable){
            return;
        }
        while (true) {
            try {
                List<Message> messages = queue.batchPopMessage(BATCH_NUM);
                if (messages != null && !messages.isEmpty()) {
                    //消费消息
                    for (Message msg : messages) {
                        handle(msg);
                        //删除已经消费的消息
                        queue.deleteMessage(msg.getReceiptHandle());
                    }
                }
                Thread.sleep(100L);
            } catch (ServiceException se) {
                logger.error("----MNS----   RequestId:{}, ErrorCode:{}, message {}" ,se.getRequestId(),se.getErrorCode(),se.getMessage());
            } catch (InterruptedException e) {
                logger.warn("Mns Interrupted",e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 初始化mns
     * 1、检测创建topic
     * 1、检测创建队列
     * 2、检测创建订阅
     * 3、绑定topic 队列 订阅 之间的关系
     * @param mnsClient
     * @param topicName
     * @param queueName
     * @param subscriptionName
     */
    private void initMns(MNSClient mnsClient,String topicName,String queueName,String subscriptionName){
        //1、获取topic
        CloudTopic topic= mnsClient.getTopicRef(topicName);
        try{
           topic.getAttribute();
        }catch (Exception e){
            //1.1、如果topic不存在则创建topic
            TopicMeta topicMeta=new TopicMeta();
            topicMeta.setTopicName(topicName);
            topic=mnsClient.createTopic(topicMeta);
        }
        //2、获取队列
        queue= mnsClient.getQueueRef(queueName);
        if(queue==null||!queue.isQueueExist()){
            //2.1、如果队列不存在,则创建队列
            QueueMeta queueMeta=new QueueMeta();
            queueMeta.setPollingWaitSeconds(queueWaitSeconds);
            queueMeta.setQueueName(queueName);
            queue=mnsClient.createQueue(queueMeta);
        }
        //3、获取订阅
        SubscriptionMeta subscriptionMeta;
        try{
            subscriptionMeta=topic.getSubscriptionAttr(subscriptionName);
        }catch (Exception e){
            subscriptionMeta=null;
        }
        if(subscriptionMeta==null){
            //3.1、如果订阅不存在,则创建订阅
            subscriptionMeta=new SubscriptionMeta();
            subscriptionMeta.setSubscriptionName(subscriptionName);
            subscriptionMeta.setNotifyContentFormat(SubscriptionMeta.NotifyContentFormat.SIMPLIFIED);
            //此属性会在推送时候进行过滤,仅推送Message的FilterTag和当前订阅FilterTag一致的消息
            subscriptionMeta.setFilterTag("filterTag");
            //3.2、绑定订阅与队列和topic关系
            subscriptionMeta.setEndpoint(topic.generateQueueEndpoint(queueName));
            //3.3、创建订阅
            topic.subscribe(subscriptionMeta);
        }

    }

    protected abstract void handle(Message popMsg);


    @Override
    public void run() {
        consume();
    }
}
