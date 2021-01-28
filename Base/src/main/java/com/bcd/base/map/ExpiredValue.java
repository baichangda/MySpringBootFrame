package com.bcd.base.map;

import com.bcd.base.exception.BaseRuntimeException;

import java.lang.ref.ReferenceQueue;

public abstract class ExpiredValue<K,V>{

    protected long expireTimeInMillis;

    public ExpiredValue(K k, V v, long expireTimeInMillis) {
        this.expireTimeInMillis = expireTimeInMillis;
    }

    public abstract V getValue();

    public abstract K getKey();

    public boolean isExpired() {
        if(expireTimeInMillis ==-1){
            return false;
        }else {
            return expireTimeInMillis < System.currentTimeMillis();
        }
    }

    public static <K,V> ExpiredValue<K,V> newExpiredValue(K k, V v, long expiredTimeInMills, int type, ReferenceQueue<ReferenceData<K,V>> referenceQueue) {
        switch (type){
            case 1:{
                return new StrongReferenceExpireValue<>(k,v,System.currentTimeMillis()+expiredTimeInMills);
            }
            case 2:{
                return new SoftReferenceExpireValue<>(k,v,System.currentTimeMillis()+expiredTimeInMills,referenceQueue);
            }
            case 3:{
                return new WeakReferenceExpireValue<>(k,v,System.currentTimeMillis()+expiredTimeInMills,referenceQueue);
            }
            default:{
                throw BaseRuntimeException.getException("type[{}] not support",type);
            }
        }
    }
}