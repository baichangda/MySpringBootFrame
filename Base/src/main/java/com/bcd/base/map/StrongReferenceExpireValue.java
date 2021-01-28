package com.bcd.base.map;

public class StrongReferenceExpireValue<K,V> extends ExpiredValue<K,V> {

    private ReferenceData<K,V> referenceData;

    public StrongReferenceExpireValue(K k,V v,long expireTimeInMillis){
        super(k,v,expireTimeInMillis);
        this.referenceData=new ReferenceData<>(k,v);
        this.expireTimeInMillis =expireTimeInMillis;
    }

    @Override
    public V getValue() {
        return referenceData.getV();
    }

    @Override
    public K getKey() {
        return referenceData.getK();
    }
}
