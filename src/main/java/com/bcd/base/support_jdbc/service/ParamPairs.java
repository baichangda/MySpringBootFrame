package com.bcd.base.support_jdbc.service;

public record ParamPairs(String field, Object val) {
    public static ParamPairs build(String field, Object val) {
        return new ParamPairs(field, val);
    }
}
