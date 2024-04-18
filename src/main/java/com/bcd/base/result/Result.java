package com.bcd.base.result;


import com.bcd.base.exception.MyException;
import com.bcd.base.util.ExceptionUtil;
import com.bcd.base.util.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @param <T>
 * @author bcd
 */
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Api调用编码(0:成功;1:失败;其他代表各种业务定义的错误)")
    private int code;
    @Schema(description = "Api调用返回的提示信息")
    private String message;
    @Schema(description = "Api调用返回的数据")
    private T data;

    public Result() {
    }

    public Result(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    public Result<T> code(int code) {
        this.code = code;
        return this;
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public static Result<?> success() {
        return new Result<>(0, null);
    }

    public static <R> Result<R> success(R data) {
        return new Result<>(0, data);
    }

    public static Result<?> fail() {
        return new Result<>(1, null);
    }

    public static <R> Result<R> fail(int code) {
        return new Result<>(code, null);
    }

    public static <R> Result<R> fail(int code, R data) {
        return new Result<>(code, data);
    }

    public static <R> Result<R> fail(R data) {
        return new Result<>(1, data);
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public static Result<?> from(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable realException = ExceptionUtil.parseRealException(throwable);
        if (realException instanceof MyException ex) {
            return Result.fail(ex.code).message(realException.getMessage());
        } else {
            return Result.fail().message(realException.getMessage());
        }
    }

}