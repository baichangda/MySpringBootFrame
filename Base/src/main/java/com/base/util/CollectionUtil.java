package com.base.util;

import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

/**
 * Created by Administrator on 2017/8/3.
 */
public class CollectionUtil {
    /**
     * 合并多个map
     * @param mergeFunction
     * @param maps
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V>Map<K,V> mergeMaps(BinaryOperator<V> mergeFunction,Map<K, V>...maps){
        Map<K,V>[] notNullMaps= Arrays.stream(maps).filter(map->map!=null).toArray((len)->new HashMap[len]);
        if(notNullMaps.length==1){
            return notNullMaps[0];
        }
        Map<K,V> resultMap= new HashMap<>();
        resultMap.putAll(notNullMaps[0]);
        Object[] otherMaps= ArrayUtils.subarray(notNullMaps,1,notNullMaps.length);
        for(Object map:otherMaps){
            if(map==null){
                continue;
            }
            ((Map<K,V>)map).forEach((k,v2)->{
                V v1=resultMap.get(k);
                if(v1==null){
                    resultMap.put(k,v2);
                }else{
                    resultMap.put(k,mergeFunction.apply(v1,v2));
                }
            });
        }
        return resultMap;
    }

}
