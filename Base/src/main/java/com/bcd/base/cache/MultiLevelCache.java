package com.bcd.base.cache;


import com.bcd.base.exception.BaseRuntimeException;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * 多级缓存结果
 * 原理:
 * 读取:从低级缓存到高级缓存,读取到以后写入到 低于读取缓存的 缓存中
 * 写入:从低级缓存到高级缓存
 * 删除:从高级缓存到低级缓存,这样做是为了避免多线程问题(一个线程读取时设置低级缓存,导致低级缓存删除不掉)
 */
@SuppressWarnings("unchecked")
public class MultiLevelCache implements Cache{
    private Cache[] caches;
    private String name;

    public MultiLevelCache(String name, Cache... caches) {
        if(caches==null||caches.length==0){
            throw BaseRuntimeException.getException("Param[caches] Can Not Be Empty!");
        }
        this.name=name;
        this.caches = caches;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        throw BaseRuntimeException.getException("Not Support!");
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper res=null;
        //1、依次从低级缓存到高级缓存中获取数据,并记录到获取到的缓存索引
        int index=-1;
        for(int i=0;i<=caches.length-1;i++){
           ValueWrapper valueWrapper= caches[i].get(key);
           if(valueWrapper!=null){
               res= valueWrapper;
               break;
           }
           index=i;
        }
        if(res==null){
            return null;
        }
        //2、依次从低到高为没有数据的缓存填充数据
        for(int i=0;i<=index;i++){
            caches[i].put(key,res.get());
        }
        return res;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T res=null;
        //1、依次从低级缓存到高级缓存中获取数据,并记录到获取到的缓存索引
        int index=-1;
        for(int i=0;i<=caches.length-1;i++){
            T val= caches[i].get(key,type);
            if(val!=null){
                res= val;
                break;
            }
            index=i;
        }
        if(res==null){
            return null;
        }
        //2、依次从低到高为没有数据的缓存填充数据
        for(int i=0;i<=index;i++){
            caches[i].put(key,res);
        }
        return res;
    }

    /**
     * 原理:
     * 只要一个缓存有值,就视为有值
     * @param key
     * @param valueLoader
     * @param <T>
     * @return
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        //1、调用get方法获取值
        ValueWrapper valueWrapper= get(key);
        //2、如果获取到了值,则返回
        if(valueWrapper!=null) {
            return (T)valueWrapper.get();
        }
        //3、如果获取不到值,则获取默认值,为所有的缓存设置默认值
        T val;
        try {
            val=valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
        put(key,val);
        return val;
    }

    @Override
    public void put(Object key, Object value) {
        //1、依次从低到高为缓存填充数据
        for(int i=0;i<=caches.length-1;i++){
            caches[i].put(key,value);
        }
    }

    /**
     * 原理是:
     * 只要一个缓存有值,就视为设置失败
     * @param key
     * @param value
     * @return
     */
    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        //1、获取key的值
        ValueWrapper valueWrapper=get(key);
        //2、如果值为null,则更新所有的缓存
        if(valueWrapper==null){
            put(key,value);
        }
        return valueWrapper;
    }

    @Override
    public void evict(Object key) {
        //1、依次从高到低移除缓存数据
        for(int i=caches.length-1;i>=0;i--){
            caches[i].evict(key);
        }
    }

    @Override
    public void clear() {
        //1、依次从高到低清除缓存
        for(int i=caches.length-1;i>=0;i--){
            caches[i].clear();
        }
    }
}
