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
     * 根据key value的条件过滤器过滤
     * 注意：会更改当前map
     *
     * @param predicate
     * @param maps
     * @param <K>
     * @param <V>s
     * @return
     */
    public static <K, V> void removeIfMaps(BiPredicate<K, V> predicate, Map<K, V>... maps) {
        if (predicate == null || maps == null || maps.length == 0) {
            return;
        }
        for (Map<K, V> map : maps) {
            if (map == null) {
                continue;
            }
            map.entrySet().removeIf(next -> predicate.test(next.getKey(), next.getValue()));
        }
    }


    /**
     * 对比两个集合,形成三个集合
     * {res1,res2,res3}
     * res1: list1比list2多的元素
     * res2: list1和list2相等的元素
     * res3: list1比list2少的元素
     * <p>
     * res1元素来自list1
     * res2内部是一个数组,数组第一个元素来自list1,第二个元素来自list2
     * res3元素来自list2
     *
     * @param list1       第一个集合
     * @param list2       第二个集合
     * @param compareFunc 对比方法
     * @param <T>         第一个集合元素类型
     * @param <U>         第二个集合元素类型
     * @return
     */
    public static <T, U> CompareListResult<T,U> compareList(List<T> list1, List<U> list2, BiFunction<T, U, Boolean> compareFunc) {
        Objects.requireNonNull(list1);
        Objects.requireNonNull(list2);
        Objects.requireNonNull(compareFunc);
        CompareListResult<T,U> result=new CompareListResult<>();
        int [] equalIndexArr2=new int[list2.size()];
        A:for (T e1 : list1) {
            for (int j = 0; j < list2.size(); j++) {
                U e2 = list2.get(j);
                if (compareFunc.apply(e1, e2)) {
                    result.add2(e1, e2);
                    equalIndexArr2[j] = 1;
                    continue A;
                }
            }
            result.add1(e1);
        }
        for (int i = 0; i < equalIndexArr2.length; i++) {
            if(equalIndexArr2[i]==0){
                result.add3(list2.get(i));
            }
        }
        return result;
    }

    public static class CompareListResult<T,U>{
        private final List<T> list1=new ArrayList<>();
        private final List<EqualData<T,U>> list2=new ArrayList<>();
        private final List<U> list3=new ArrayList<>();

        private void add1(T t){
            list1.add(t);
        }
        private void add2(T t,U u){
            list2.add(new EqualData<>(t,u));
        }
        private void add3(U u){
            list3.add(u);
        }

        public List<T> getList1() {
            return list1;
        }

        public List<EqualData<T, U>> getList2() {
            return list2;
        }

        public List<U> getList3() {
            return list3;
        }
    }

    public static class EqualData<T,U>{
        private final T e1;
        private final U e2;

        public EqualData(T e1, U e2) {
            this.e1 = e1;
            this.e2 = e2;
        }

        public T getE1() {
            return e1;
        }

        public U getE2() {
            return e2;
        }
    }

    public static void main(String[] args) {
        CollectionUtil.CompareListResult<Integer, Integer> result = CollectionUtil.compareList(Arrays.asList(1, 2, 3), Arrays.asList(3, 5, 7), Integer::equals);
        result.getList1().forEach(e-> System.out.print(e+" "));
        System.out.println();
        result.getList2().forEach(e-> System.out.print(e.getE1()+":"+e.getE2()+" "));
        System.out.println();
        result.getList3().forEach(e-> System.out.print(e+" "));
    }

}
