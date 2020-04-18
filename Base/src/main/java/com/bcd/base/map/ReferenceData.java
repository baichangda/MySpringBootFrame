package com.bcd.base.map;

class ReferenceData<K,V>{
    K key;
    V val;

    public ReferenceData(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public K getKey() {
        return key;
    }

    public V getVal() {
        return val;
    }
}