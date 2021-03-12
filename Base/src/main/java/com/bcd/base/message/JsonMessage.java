package com.bcd.base.message;


import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Created by dave on 16/2/13.
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class JsonMessage<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "Api是否调用成功(true/false)")
    private boolean result;
    @Schema(description = "Api调用失败时提示信息")
    private String message;
    @Schema(description = "Api调用失败时错误编码")
    private String code;
    @Schema(description = "Api调用返回的数据")
    private T data;

    public JsonMessage() {
    }

    public JsonMessage(boolean result) {
        this.result = result;
    }

    public static <R> JsonMessage<R> success() {
        return new JsonMessage<>(true);
    }

    public static <R> JsonMessage<R> fail() {
        return new JsonMessage<>(false);
    }

    public boolean isResult() {
        return result;
    }

    public <R>JsonMessage<R> withResult(boolean result) {
        this.result = result;
        return (JsonMessage<R>)this;
    }

    public String getMessage() {
        return message;
    }

    public <R>JsonMessage<R> withMessage(String message) {
        this.message = message;
        return (JsonMessage<R>)this;
    }

    public String getCode() {
        return code;
    }

    public <R>JsonMessage<R> withCode(String code) {
        this.code = code;
        return (JsonMessage<R>)this;
    }

    public T getData() {
        return data;
    }

    public <R>JsonMessage<R> withData(T data) {
        this.data = data;
        return (JsonMessage<R>)this;
    }
}
