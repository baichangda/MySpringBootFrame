package com.base.json;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by dave on 16/2/13.
 * @param <T>
 */
public class JsonMessage<T> {
    private boolean result;
    private String message = "";
    private String error = "";
    private String code="";
    private T data;

    public JsonMessage() { }

    public JsonMessage(boolean result) {
        this.result = result;
    }

    public JsonMessage(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public JsonMessage(boolean result, String message, String error) {
        this(result, message);
        this.error = error;
    }

    public JsonMessage(boolean result, String message,String code, String error) {
        this(result, message, error);
        this.code = code;
    }

    public JsonMessage(boolean result, String message,String code, String error, T data) {
        this(result, message, error,code);
        this.data = data;
    }

    public static <T> JsonMessage<T> successed() {
        JsonMessage<T> jsonMessage = new JsonMessage<>(true);
        jsonMessage.setCode(String.valueOf(HttpServletResponse.SC_OK));
        return jsonMessage;
    }

    public static <T> JsonMessage<T> successed(T data) {
        JsonMessage<T> jsonMessage = successed();
        jsonMessage.setData(data);
        return jsonMessage;
    }

    public static <T> JsonMessage<T> successed(T data,String message) {
        JsonMessage<T> jsonMessage =successed(data);
        jsonMessage.setMessage(message);
        return jsonMessage;
    }

    public static <T> JsonMessage<T> failed(String message) {
        JsonMessage<T> jsonMessage = new JsonMessage<>(false, message);
        return jsonMessage;
    }

    public static <T> JsonMessage<T> failed(String message,String code) {
        JsonMessage<T> jsonMessage = new JsonMessage<>(false, message,code);
        return jsonMessage;
    }

    public static <T> JsonMessage<T> failed(String message,String code,String error) {
        JsonMessage<T> jsonMessage = new JsonMessage<>(false, message,error,code);
        return jsonMessage;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isResult() {
        return result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
