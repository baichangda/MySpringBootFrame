package com.bcd.base.map;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakReferenceExpireValue<K,V> extends ExpiredValue<K,V> {

    private final WeakReference<ReferenceData<K,V>> reference;

    public WeakReferenceExpireValue(K k, V v, long expireTimeInMills, ReferenceQueue<ReferenceData<K,V>> referenceQueue){
        super(k, v, expireTimeInMills);
        this.reference =new WeakReference<>(new ReferenceData<>(k, v),referenceQueue);
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
