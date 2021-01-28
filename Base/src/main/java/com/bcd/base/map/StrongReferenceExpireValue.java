package com.bcd.base.map;

public class StrongReferenceExpireValue<K,V> extends ExpiredValue<K,V> {

    private ReferenceData<K,V> referenceData;

    public StrongReferenceExpireValue(K k,V v,long expireTimeInMills){
        super(k,v,expireTimeInMills);
        this.referenceData=new ReferenceData<>(k,v);
        this.expireTimeInMills=expireTimeInMills;
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
