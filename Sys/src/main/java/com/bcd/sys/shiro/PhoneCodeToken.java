package com.bcd.sys.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class PhoneCodeToken implements AuthenticationToken {

    private String phone;
    private String code;

    public PhoneCodeToken(String phone, String code) {
        this.phone = phone;
        this.code = code;
    }

    public String getPhone() {
        return phone;
    }

    public String getCode() {
        return code;
    }

    @Override
    public Object getPrincipal() {
        return phone;
    }

    @Override
    public Object getCredentials() {
        return code;
    }
}
