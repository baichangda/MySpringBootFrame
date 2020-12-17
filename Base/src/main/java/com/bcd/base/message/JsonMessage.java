package com.bcd.base.message;


import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Created by dave on 16/2/13.
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class JsonMessage<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("Api是否调用成功(true/false)")
    private boolean result;
    @ApiModelProperty("Api调用失败时提示信息")
    private String message = "";
    @ApiModelProperty("Api调用失败时错误编码")
    private String code = "";
    @ApiModelProperty("Api调用返回的数据(Json字符串)")
    private T data;

    public JsonMessage() {
    }

    public JsonMessage(boolean result) {
        this.result=result;
    }

    public static <T> JsonMessage<T> success() {
        return new JsonMessage<>(true);
    }

    public static <T> JsonMessage<T> fail() {
        return new JsonMessage<>(false);
    }

    public boolean isResult() {
        return result;
    }

    public JsonMessage<T> withResult(boolean result) {
        this.result = result;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public JsonMessage<T> withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getCode() {
        return code;
    }

    public JsonMessage<T> withCode(String code) {
        this.code = code;
        return this;
    }

    public T getData() {
        return data;
    }

    public <R> JsonMessage<R> withData(T data) {
        this.data = data;
        return (JsonMessage<R>)this;
    }
}
