package com.bcd.base.map;



import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

/**
 * 过期value
 * 其中存储了插入时候时间戳
 * 此value为软引用,在old gc时候如果发现空间不足则会回收掉
 * @param <K,V>
 */
public class ExpireSoftReferenceValue<K,V> extends SoftReference<ReferenceData<K,V>> {
    private long expireTime;

    private K key;

    public ExpireSoftReferenceValue(long expireTime, ReferenceData<K,V> referenceData, ReferenceQueue<? super ReferenceData<K,V>> reference) {
        super(referenceData,reference);
        this.expireTime = expireTime;
        this.key=referenceData.getKey();
    }

    public K getKey() {
        return key;
    }

    public V getVal() {
        ReferenceData<K,V> data=get();
        if(data==null){
            return null;
        }else{
            if(isExpired()){
                return null;
            }else{
                return data.getVal();
            }
        }
    }

    public long getExpireTime() {
        return expireTime;
    }

    private boolean isExpired() {
        return expireTime < System.currentTimeMillis();
    }

    public static void main(String[] args) throws InterruptedException {
        ReferenceQueue<ReferenceData<String,String>> referenceQueue=new ReferenceQueue<>();
        ExpireSoftReferenceValue<String,String> reference=new ExpireSoftReferenceValue<>(1000L,new ReferenceData<>("a","b"),referenceQueue);
        Executors.newSingleThreadExecutor().execute(()->{
            while (true){
                try {
                    Reference<? extends ReferenceData<String,String>> res= referenceQueue.remove();
                    System.out.println("remove:"+res.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        System.gc();
        while(true){
            Thread.sleep(1000);
            System.out.println(reference.get());
        }
    }


}
