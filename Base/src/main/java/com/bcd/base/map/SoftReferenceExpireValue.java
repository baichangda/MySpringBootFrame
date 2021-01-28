package com.bcd.base.map;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftReferenceExpireValue<K,V> extends ExpiredValue<K,V> {

    private SoftReference<ReferenceData<K,V>> reference;

    public SoftReferenceExpireValue(K k, V v, long expireTimeInMills, ReferenceQueue<ReferenceData<K,V>> referenceQueue){
        super(k, v, expireTimeInMills);
        this.reference =new SoftReference<>(new ReferenceData<>(k, v),referenceQueue);
        this.expireTimeInMills=expireTimeInMills;
    }

    @Override
    public V getValue() {
        ReferenceData<K,V> referenceData= reference.get();
        return referenceData==null?null:referenceData.getV();
    }

    @Override
    public K getKey() {
        ReferenceData<K,V> referenceData= reference.get();
        return referenceData==null?null:referenceData.getK();
    }
}
