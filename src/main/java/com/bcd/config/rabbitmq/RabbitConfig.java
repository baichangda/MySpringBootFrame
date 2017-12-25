package com.bcd.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;

//@Configuration
public class RabbitConfig {
    /**
     * route key
     */
    private final String dataRouteKey="device.data.landu";

    /**
     * 队列
     * @return
     */
    @Bean
    public Queue dataQueue() {
        return new Queue("dataQueue",true);
    }

    /**
     * Exchange
     * @return
     */
    @Bean
    public TopicExchange dataExchange(){
        return new TopicExchange("iov_exchange",false,false);
    }

    /**
     * 绑定关系
     * @param dataQueue
     * @param dataExchange
     * @return
     */
    @Bean
    public Binding binding(Queue dataQueue,TopicExchange dataExchange){
        return BindingBuilder.bind(dataQueue).to(dataExchange).with(dataRouteKey);
    }


    /**
     * 指定rabbitmq的监听器工厂、指定转换器
     * @param connectionFactory
     * @return
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new MyMessageConverter());
        return factory;
    }
}