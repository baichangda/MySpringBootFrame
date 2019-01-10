package com.bcd.base.util;


import com.bcd.base.exception.BaseRuntimeException;

import java.util.*;
import java.util.function.*;

/**
 * Created by Administrator on 2017/8/3.
 */
@SuppressWarnings("unchecked")
public class CollectionUtil {

    /**
     * 合并多个集合的元素
     * @param mergeFun 如果合并的方法为空,则无条件合并两个集合的元素,且返回结果集类型为提供集合数组的第一个非空的集合
     * @param lists
     * @param <T>
     * @return
     */
    public static <T>List<T> mergeList(BinaryOperator<List<T>> mergeFun,Supplier<? extends List> listSupplier, List<T> ... lists){
        List<T> resList= listSupplier.get();
        //1、如果集合参数为null或者长度为0则直接返回
        if(lists==null||lists.length==0){
            return resList;
        }
        //2、如果非空的集合为0则返回非null集合第一个元素,为1则直接返回这个元素
        List<T>[] notEmptyLists= Arrays.stream(lists).filter(e->e!=null&&!e.isEmpty()).toArray(len->new List[len]);
        if(notEmptyLists.length==0){
            return resList;
        }else if(notEmptyLists.length==1){
            resList.addAll(notEmptyLists[0]);
            return resList;
        }
        //3、如果合并的方法为空,则无条件合并两个集合的元素
        if(mergeFun==null){
            for(int i=0;i<=notEmptyLists.length-1;i++){
                resList.addAll(notEmptyLists[i]);
            }
        }else{
            List<T> tempList=notEmptyLists[0];
            for(int i=1;i<=notEmptyLists.length-1;i++){
                tempList=mergeFun.apply(tempList,notEmptyLists[i]);
            }
            resList.addAll(tempList);
        }
        return resList;
    }


    public static <K,A,V,B>Map<A,B> replaceMap(Function<K,A> keyFun, Function<V,B> valFun, Supplier<? extends Map<A,B>> mapSupplier, Map<K,V> map){
        return replaceMap(keyFun,valFun,null,mapSupplier,map);
    }

    public static <K,A,V,B>Map<A,B>[] replaceMaps(Function<K,A> keyFun,Function<V,B> valFun,Supplier<? extends Map<A,B>> mapSupplier,Map<K,V> ... maps){
        return replaceMaps(keyFun,valFun,null,mapSupplier,maps);
    }

    public static <K,A,V,B>Map<A,B> replaceMap(Function<K,A> keyFun,Function<V,B> valFun,BinaryOperator<B> mergeFun,Supplier<? extends Map<A,B>> mapSupplier,Map<K,V> map){
        return replaceMaps(keyFun,valFun,mergeFun,mapSupplier,map)[0];
    }

    /**
     * 根据key value合并器生成新的map
     * 如果 keyFun 或者 valFun 为空，则生成的实际类型与原数据集的类型一样，忽略泛型定义
     * @param keyFun key的合并方法
     * @param valFun val的合并方法
     * @param mergeFun 如果遇见相同的key,如何处理val(如果为null,则会进行替换处理)
     * @param mapSupplier 结果map对象提供
     * @param maps 需要合并的map
     * @param <K>
     * @param <A>
     * @param <V>
     * @param <B>
     * @return
     */
    public static <K,A,V,B>Map<A,B>[] replaceMaps(Function<K,A> keyFun,Function<V,B> valFun,BinaryOperator<B> mergeFun,Supplier<? extends Map<A,B>> mapSupplier,Map<K,V> ... maps){
        if(maps==null||maps.length==0){
            throw BaseRuntimeException.getException("[CollectionUtil.replaceMaps],Param[maps] Must Not Be Empty!");
        }
        if(keyFun==null&&valFun==null){
            throw BaseRuntimeException.getException("[CollectionUtil.replaceMaps],Either Param[keyFun] or Param[valFun] Must Not Be Null!");
        }
        Map<A,B>[] resultMaps=new Map[maps.length];
        for(int i=0;i<=maps.length-1;i++){
            Map<A,B> resultMap=mapSupplier.get();
            maps[i].forEach((k,v)->{
                Object resKey;
                Object resVal;
                if(keyFun==null){
                    resKey=k;
                }else{
                    resKey=keyFun.apply(k);
                }
                if(valFun==null){
                    resVal=v;
                }else{
                    resVal=valFun.apply(v);
                }
                if(resultMap.containsKey(resKey)){
                    if(mergeFun==null){
                        resultMap.put((A)resKey,(B)resVal);
                    }else{
                        B mergeRes= mergeFun.apply(resultMap.get(resKey),(B)resVal);
                        resultMap.put((A)resKey,mergeRes);
                    }
                }else{
                    resultMap.put((A)resKey,(B)resVal);
                }
            });
            resultMaps[i]=resultMap;
        }

        return resultMaps;
    }


    /**
     * 根据key value的条件过滤器过滤
     * 注意：会更改当前map
     * @param predicate
     * @param maps
     * @param <K>
     * @param <V>s
     * @return
     */
    public static <K,V>void removeIfMaps(BiPredicate<K,V> predicate, Map<K,V> ... maps){
        if(predicate==null||maps==null||maps.length==0){
            return;
        }
        for (Map<K, V> map : maps) {
            if(map==null){
                continue;
            }
            Set<K> removeKeySet=new HashSet<>();
            map.forEach((k,v)->{
                if(predicate!=null&&predicate.test(k,v)){
                    removeKeySet.add(k);
                }
            });
            removeKeySet.forEach(k->map.remove(k));
        }
    }


    /**
     * 合并多个map
     * @param mergeFunction
     * @param maps
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V>Map<K,V> mergeMaps(BinaryOperator<V> mergeFunction,Supplier<? extends Map<K,V>> mapSupplier,Map<K, V>...maps){
        //1、如果传入的map数组长度为0或者1,根据情况直接返回
        if(maps.length==0){
            return null;
        }else if(maps.length==1){
            return maps[0];
        }
        //2、获取非空的map
        Map<K,V>[] notNullMaps= Arrays.stream(maps).filter(map->map!=null&&map.size()>0).<Map>toArray(len->new Map[len]);
        //3、如果非空的map长度为0则返回空的map;为1则返回这个map
        if(notNullMaps.length==0){
            return new LinkedHashMap<>();
        }else if(notNullMaps.length==1){
            return notNullMaps[0];
        }
        //4、用指定的操作方法合并map
        Map<K,V>[] resultMap= new Map[]{mapSupplier.get()};
        resultMap[0].putAll(notNullMaps[0]);
        Map<K,V>[] otherMaps=new Map[notNullMaps.length-1];
        System.arraycopy(notNullMaps,1,otherMaps,0,otherMaps.length);
        for(Map<K,V> map:otherMaps){
            if(map==null||map.size()==0){
                continue;
            }
            map.forEach((k,v2)->{
                V v1=resultMap[0].get(k);
                if(v1==null){
                    resultMap[0].put(k,v2);
                }else{
                    resultMap[0].put(k,mergeFunction.apply(v1,v2));
                }
            });
        }
        return resultMap[0];
    }

    /**
     * 对比两个集合,形成三个集合
     * {res1,res2,res3}
     * res1: list1比list2多的元素
     * res2: list1和list2相等的元素
     * res3: list1比list2少的元素
     *
     * res1元素来自list1
     * res2内部是一个数组,数组第一个元素来自list1,第二个元素来自list2
     * res3元素来自list2
     *
     *
     * @param list1 第一个集合
     * @param list2 第二个集合
     * @param compareFunc 对比方法
     * @param <T> 第一个集合元素类型
     * @param <U> 第二个集合元素类型
     * @return
     */
    public static <T,U>List[] compareList(List<T> list1, List<U> list2, BiFunction<T,U,Boolean> compareFunc){
        List<T> res1=list1==null?new ArrayList<>():new ArrayList<>(list1);
        List<Object[]> res2=new ArrayList<>();
        List<U> res3=list2==null?new ArrayList<>():new ArrayList<>(list2);
        if(list1!=null&&!list1.isEmpty()&&list2!=null&&!list2.isEmpty()){
            for (T t : list1) {
                for(U u : list2){
                    if(compareFunc.apply(t,u)){
                        res2.add(new Object[]{res1.remove(t),res3.remove(u)});
                    }
                }
            }
        }
        return new List[]{res1,res2,res3};
    }

}
