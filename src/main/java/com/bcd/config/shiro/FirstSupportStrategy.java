package com.bcd.config.shiro;

import com.bcd.base.exception.BaseRuntimeException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.ShortCircuitIterationException;
import org.apache.shiro.realm.Realm;

import java.util.Collection;

/**
 * 当多个realm时候,遇到第一个support realm执行验证,验证完以后退出
 */
public class FirstSupportStrategy implements AuthenticationStrategy {
    @Override
    public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
        return null;
    }

    @Override
    public AuthenticationInfo beforeAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        //如果检测到已经有结果则打断循环
        if (aggregate == null) {
            return null;
        } else {
            throw new ShortCircuitIterationException();
        }
    }

    @Override
    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
        if (t == null) {
            return singleRealmInfo;
        } else {
            if (t instanceof AuthenticationException) {
                throw (AuthenticationException) t;
            } else {
                throw BaseRuntimeException.getException(t);
            }
        }
    }

    @Override
    public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        return aggregate;
    }
}
