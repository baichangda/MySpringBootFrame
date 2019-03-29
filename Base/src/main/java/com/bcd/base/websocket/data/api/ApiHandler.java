package com.bcd.base.websocket.data.api;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.message.JsonMessage;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ApiHandler {

    public static Map<String,ApiHandler> NAME_TO_HANDLER_MAP=new HashMap<>();
    protected Method method;
    protected Class[] paramTypes;
    protected Class returnType;
    protected Object obj;

    public ApiHandler(Method method,Object obj) {
        this.method = method;
        this.obj=obj;
        paramTypes =method.getParameterTypes();
        returnType=method.getReturnType();
    }


    public JsonMessage execute(String paramJson){
        JsonMessage jsonMessage;
        try {
            //1、转换参数类型
            Object[] params = convertParams(paramJson);
            //2、调用方法
            Object res=method.invoke(obj, params);
            //3、转换返回结果
            if (Void.TYPE.isAssignableFrom(returnType)) {
                return null;
            }else{
                jsonMessage = JsonMessage.success(res);
            }
        } catch (Exception e) {
            //4、转换异常结果
            jsonMessage = BaseRuntimeException.getException(e).toJsonMessage();
        }
        return jsonMessage;
    }

    private Object[] convertParams(String paramJson) throws IOException {
        JsonNode jsonNode= JsonUtil.GLOBAL_OBJECT_MAPPER.readTree(paramJson);
        if(jsonNode.size()!=paramTypes.length){
            throw BaseRuntimeException.getException("Param Count Error");
        }
        Object[] res=new Object[paramTypes.length];
        for(int i=0;i<=paramTypes.length-1;i++){
            JsonNode cur=jsonNode.get(i);
            res[i]= JsonUtil.GLOBAL_OBJECT_MAPPER.convertValue(cur,paramTypes[i]);
        }
        return res;
    }
}
