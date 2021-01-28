package com.bcd.base.map;

public class ReferenceData<K,V> {
    private final K k;
    private final V v;

    public ReferenceData(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getK() {
        return k;
    }

    public V getV() {
        return v;
    }
}
