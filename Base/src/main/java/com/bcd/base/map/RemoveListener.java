package com.bcd.base.map;

public interface RemoveListener<K,V>{
    void onRemove(K k,V v);
}