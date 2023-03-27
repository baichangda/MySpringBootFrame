package com.bcd.base.message;


import com.bcd.base.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by dave on 16/2/13.
 *
 * @param <T>
 */
public class JsonMessage<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Api调用编码(0:成功;1:通用错误;其他代表各种业务定义的错误)")
    private int code;
    @Schema(description = "Api调用失败时提示信息")
    private String message;
    @Schema(description = "Api调用返回的数据")
    private T data;

    public JsonMessage() {
    }

    public JsonMessage(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public JsonMessage<T> message(String message) {
        this.message = message;
        return this;
    }

    public JsonMessage<T> code(int code) {
        this.code = code;
        return this;
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public static JsonMessage<?> success() {
        return new JsonMessage<>(0, null);
    }

    public static <R> JsonMessage<R> success(R data) {
        return new JsonMessage<>(0, data);
    }

    public static JsonMessage<?> fail() {
        return new JsonMessage<>(1, null);
    }

    public static <R> JsonMessage<R> fail(int code) {
        return new JsonMessage<>(code, null);
    }

    public static <R> JsonMessage<R> fail(int code, R data) {
        return new JsonMessage<>(code, data);
    }

    public static <R> JsonMessage<R> fail(R data) {
        return new JsonMessage<>(1, data);
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

}