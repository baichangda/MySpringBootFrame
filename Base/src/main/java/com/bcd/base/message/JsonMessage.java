package com.bcd.base.message;


import com.bcd.base.util.JsonUtil;
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

    public JsonMessage<T> withData(T data) {
        this.data = data;
        return this;
    }

    public String toJson(){
        return JsonUtil.toJson(this);
    }
}