package com.bcd.base.websocket.client;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.map.ExpireCallBackConcurrentHashMap;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import com.bcd.base.websocket.data.WebSocketData;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * webSocket客户端,存在如下机制
 * 1、断线重连机制(当连接断开以后会过10s后自动连接 (如果检测到发送时间距离当前时间1分钟之内) )
 * @param <T>
 */
@SuppressWarnings("unchecked")
public abstract class BaseJsonWebSocketClient<T> extends BaseTextWebSocketClient {

    public ExpireCallBackConcurrentHashMap<String,Consumer<String>> sn_to_callBack_map;

    public BaseJsonWebSocketClient(String url) {
        super(url);
        //扫描间隔为1s
        this.sn_to_callBack_map =new ExpireCallBackConcurrentHashMap<>(1000L);
        this.sn_to_callBack_map.init();
    }

    /**
     * @param param 参数
     * @param consumer webSocket回调(json字符串参数)
     * @param timeOutMills 超时时间(毫秒)
     * @param timeOutCallBack  webSocket超时回调
     * @param clazzs 结果泛型类型数组
     * @param <R>
     * @return 返回null表示发送失败;正常情况下返回发送过去的数据
     */
    public <R>WebSocketData<T> sendMessage(T param, Consumer<WebSocketData<R>> consumer, long timeOutMills, BiConsumer<String,Consumer<WebSocketData<R>>> timeOutCallBack, Class... clazzs){
        //1、开始
        WebSocketData<T> paramWebSocketData=new WebSocketData<>(RandomStringUtils.randomAlphabetic(32),param);
        logger.debug("Start WebSocket SN["+paramWebSocketData.getSn()+"]");
        //1.1、绑定回调
        sn_to_callBack_map.put(paramWebSocketData.getSn(), (v) -> {
            try {
                JavaType resType;
                if(clazzs.length==1){
                    resType= TypeFactory.defaultInstance().constructParametricType(WebSocketData.class,clazzs);
                }else {
                    JavaType tempType=null;
                    for (int i = clazzs.length - 2; i >= 0; i--) {
                        if (tempType == null) {
                            tempType = TypeFactory.defaultInstance().constructParametricType(clazzs[i], clazzs[i + 1]);
                        } else {
                            tempType = TypeFactory.defaultInstance().constructParametricType(clazzs[i], tempType);
                        }
                    }
                    resType= TypeFactory.defaultInstance().constructParametricType(WebSocketData.class,tempType);
                }
                WebSocketData<R> webSocketData = JsonUtil.GLOBAL_OBJECT_MAPPER.readValue(v, resType);
                consumer.accept(webSocketData);
            } catch (IOException e) {
                throw BaseRuntimeException.getException(e);
            }
        }, timeOutMills, (k, v) -> {
            logger.info("TimeOut WebSocket SN[" + paramWebSocketData.getSn() + "]");
            timeOutCallBack.accept(k, consumer);
        });
        //2、发送信息
        boolean sendRes= sendMessage(paramWebSocketData);
        //3、如果发送失败,则移除回调
        if(sendRes){
            return paramWebSocketData;
        }else{
            sn_to_callBack_map.remove(paramWebSocketData.getSn());
            return null;
        }
    }

    /**
     * 发送数据
     * @param param
     * @return
     */
    protected boolean sendMessage(WebSocketData<T> param){
        String message = JsonUtil.toJson(param);
        boolean res= sendMessage(message);
        if(res){
            logger.debug("Send WebSocket SN[" + param.getSn() + "]");
        }
        return res;
    }

    /**
     * 当收到数据时候触发
     * @param data
     */
    public void onMessage(String data){
        try {
            JsonNode jsonNode = JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(data);
            String sn=jsonNode.get("sn").asText();
            logger.info("Receive WebSocket SN[" + sn + "]");
            //1、取出流水号
            Consumer<String> consumer = sn_to_callBack_map.remove(sn);
            //2、触发回调
            if (consumer == null) {
                logger.warn("Receive No Consumer Message SN[" + sn + "]");
            } else {
                consumer.accept(data);
            }
        }catch (Exception e){
            ExceptionUtil.printException(e);
        }
    }

    /**
     * 阻塞调用webSocket请求
     * @param paramData
     * @param timeOut
     * @param clazzs 返回参数泛型类型数组,只支持类型单泛型,例如:
     *               <WebData<VehicleBean>> 传参数 WebData.class,VehicleBean.class
     * @return 返回null表示发送超时
     */
    public <R>WebSocketData<R> blockingRequest(T paramData, long timeOut, Class ... clazzs){
        CountDownLatch countDownLatch=new CountDownLatch(1);
        WebSocketData<R>[] resData=new WebSocketData[1];
        WebSocketData<T> sendWebSocketData=sendMessage(paramData,(WebSocketData<R> res)->{
                resData[0]=res;
                countDownLatch.countDown();
        }, timeOut,(k, v)->{
            countDownLatch.countDown();
        },clazzs);
        if(sendWebSocketData==null){
            throw BaseRuntimeException.getException("Session Closed,WebSocket Send Failed");
        }else{
            try {
                countDownLatch.await();
                return resData[0];
            } catch (InterruptedException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }

    /**
     * 是否支持分片信息
     * @return
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
}
