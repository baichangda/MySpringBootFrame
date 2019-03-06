package com.bcd.base.map;

public class ExpireKey<K,V> {
    private K key;
    private ExpireValue<V> expireValue;

    public ExpireKey(K key, ExpireValue<V> expireValue) {
        this.key = key;
        this.expireValue = expireValue;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public ExpireValue<V> getExpireValue() {
        return expireValue;
    }

    public void setExpireValue(ExpireValue<V> expireValue) {
        this.expireValue = expireValue;
    }
}
