package com.base.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/5/12.
 */
@SuppressWarnings("unchecked")
public class JsonUtil {




    /**
     * 获取obj默认JSON字符串
     *
     * @param obj
     * @return
     */
    public static String toDefaultJSONString(Object obj, SerializeFilter... filters) {
        return JSONObject.toJSONString(obj, SerializeConfig.globalInstance, filters, SerializerFeature.DisableCircularReferenceDetect);
    }


    public static String toDefaultJSONString(Object obj) {
        return toDefaultJSONString(obj);
    }

    /**
     * 解析过滤字符串数组成json过滤器
     * 依次循环过滤字符串数组，找当前 clazz 参数对应的字段，最后形成找到结果的过滤器
     * example:
     * parseJsonFiltersByParam( EnumTypeDTO.class , new String[]{"enumItemDTOSet.enumTypeDTO"} )
     * 生成
     * SimplePropertyPreFilter filter1=new SimplePropertyPreFilter(EnumItemDTO.class);
     * filter1.getExcludes().add("enumTypeDTO");
     * return new SimplePropertyPreFilter[]{filter1};
     *
     * @param clazz
     * @param filterStrArr
     * @return
     */
    public static SimplePropertyPreFilter[] parseJsonFiltersByParam(Class clazz,String [] filterStrArr){
        //1、非空验证
        if(filterStrArr==null||filterStrArr.length==0){
            return null;
        }
        //2、构造过滤器集合，供返回
        Map<String,SimplePropertyPreFilter> filterMap=new HashMap<>();
        //3、循环过滤器字符串数组，解析每一个字符串，形成对应的过滤器
        A:for(int i=0;i<=filterStrArr.length-1;i++){
            //4、过滤字符串非空验证
            if(filterStrArr[i]==null||filterStrArr[i].trim().equals("")){
                continue;
            }
            //5、定义字段数组，接收split字符串的结果
            String[] fieldStrArr;
            if(filterStrArr[i].indexOf('.')==-1){
                fieldStrArr=new String[]{filterStrArr[i]};
            }else {
                fieldStrArr=filterStrArr[i].split("\\.");
            }

            if(filterStrArr!=null&&filterStrArr.length>0){
                //6、定义当前class和当前field
                Class curClazz=clazz;
                Field curField;
                //7、循环字段数组
                for(int j=0;j<=fieldStrArr.length-1;j++){
                    //8、结合当前类、字段数组，得到当前的字段；取不倒字段则证明过滤器字符串错误，跳过外层循环
                    //8.1、如果是Map,则直接构造过滤器返回
                    if(Map.class.isAssignableFrom(clazz)){
                        SimplePropertyPreFilter filter= filterMap.get(Map.class.getName());
                        if(filter==null){
                            filter=new SimplePropertyPreFilter(Map.class);
                        }
                        filter.getExcludes().add(fieldStrArr[j]);
                        filterMap.put(Map.class.getName(),filter);
                    }
                    //8.2、否则则进行字段的验证
                    try {
                        curField = curClazz.getField(fieldStrArr[j]);
                    } catch (NoSuchFieldException e) {
                        try {
                            curField = curClazz.getDeclaredField(fieldStrArr[j]);
                        } catch (NoSuchFieldException e1) {
                            continue A;
                        }
                    }
                    //9、如果是循环结束，则形成过滤器并加入集合中
                    if(j==fieldStrArr.length-1){
                        String key=curClazz.getName();
                        //9.1、去除重复filter
                        SimplePropertyPreFilter filter= filterMap.get(key);
                        if(filterMap.get(key)==null){
                            filter= new SimplePropertyPreFilter(curClazz);
                        }
                        filter.getExcludes().add(curField.getName());
                        filterMap.put(key,filter);
                    }else{
                        //10、取到当前字段，如果是集合类，则需要取到集合中泛型类型，同时将当前类设置成泛型类型
                        if(Collection.class.isAssignableFrom(curField.getType())){
                            curClazz= (Class)((ParameterizedType)curField.getGenericType()).getActualTypeArguments()[0];
                        }else{
                            //11、如果是非集合类，则取字段类型，如果字段类型是基础类型，则跳过外层循环,否则将当前类设置成字段类型
                            curClazz= curField.getType();
                            for(int n=0;n<=BeanUtil.BASE_DATA_TYPE.length-1;n++){
                                if(BeanUtil.BASE_DATA_TYPE[n].isAssignableFrom(curClazz)){
                                    continue A;
                                }
                            }
                        }
                    }
                }
            }
        }

        Collection<SimplePropertyPreFilter> filterCollection= filterMap.values();
        //12、合并重复类的filter
        return filterCollection.toArray(new SimplePropertyPreFilter[filterCollection.size()]);
    }

    /**
     * 支持多个过滤器类型
     * @param paramArr
     * @return
     */
    public static SimplePropertyPreFilter[] parseJsonFiltersByParam(Object[][] paramArr){
        List<SimplePropertyPreFilter> simplePropertyPreFilterList=new ArrayList<>();
        //1、循环调用生成filter数组
        for(int i=0;i<=paramArr.length-1;i++){
            Class clazz=(Class)paramArr[i][0];
            String [] filterStrArr=(String [])paramArr[i][1];
            SimplePropertyPreFilter curSimplePropertyPreFilter[]= parseJsonFiltersByParam(clazz,filterStrArr);
            simplePropertyPreFilterList.addAll(Arrays.asList(curSimplePropertyPreFilter));
        }
        //2、合并多次调用的返回结果,将相同类的filter整合在一起
        Map<String,SimplePropertyPreFilter> filterMap= simplePropertyPreFilterList.stream().collect(Collectors.toMap(
                (filter)->filter.getClazz().getName(),
                (filter)->filter,
                (filter1,filter2)->{
                    filter1.getExcludes().addAll(filter2.getExcludes());
                    return filter1;
                }
        ));
        return filterMap.values().toArray(new SimplePropertyPreFilter[filterMap.size()]);
    }


    /**
     * 深度为1的过滤器
     * 根据当前对象类型：
     * 1.将当前对象中带ManyToMany和OneToMany注解的属性获取,这些属性的对象类型为集合类型,
     *   然后根据集合里的参数对象类型得到此对象类型的简单属性过滤器；
     * 2.将当前对象中带ManyToOne和OneToOne注解的属性获取,获取这些类型的深度为0属性过滤器
     *
     * remarks:如果遇到自关联属性、则忽略当前属性过滤器
     *
     * @param clazz
     * @return
     */
    public static SimplePropertyPreFilter[] getOneDeepJsonFilter(Class clazz){
        List<SimplePropertyPreFilter> simplePropertyPreFilterList = new ArrayList<>();
        //1、获取当前类型所有带ManyToMany和OneToMany注解的属性集合
        Class[] annotationArr = new Class[]{ManyToMany.class, OneToMany.class};
        List<Field> collectionFieldList= BeanUtil.getFieldList(clazz,annotationArr);
        //2.获取当前类型所有带ManyToOne和OneToOne的注解的属性集合
        List<Field> objectFieldList = BeanUtil.getFieldList(clazz, new Class[]{ManyToOne.class, OneToOne.class});
        //3、遍历所有带ManyToMany和OneToMany注解的属性集合
        for (Field collectionField: collectionFieldList){
            //3.1 拿到属性为集合的类型
            Type genericType = collectionField.getGenericType();
            //3.2 得到该集合里参数对象的类型
            Class fieldClazz = (Class)(((ParameterizedType) genericType).getActualTypeArguments()[0]);
            //3.3 如果遇到自关联属性、则忽略当前属性过滤器
            if(fieldClazz==clazz){
                continue;
            }
            //3.4 获取集合里参数对象类型的简单属性过滤器，放入simplePropertyPreFilterList中
            simplePropertyPreFilterList.add(getSimpleJsonFilter(fieldClazz));
        }
        //4.遍历所有带ManyToOne和OneToOne的注解的属性集合
        objectFieldList.forEach(objectField -> {
            //4.1 得到该属性的对象类型
            Class fieldClazz = objectField.getType();
            //4.2 如果遇到自关联属性、则忽略当前属性过滤器
            if(fieldClazz==clazz){
                return;
            }
            //4.3 添加简单属性过滤器`
            CollectionUtils.addAll(simplePropertyPreFilterList,getZeroDeepJsonFilter(clazz));
        });
        //5、合并多次调用的返回结果,将相同类的filter整合在一起
        Map<String,SimplePropertyPreFilter> filterMap= simplePropertyPreFilterList.stream().collect(Collectors.toMap(
                (filter)->filter.getClazz().getName(),
                (filter)->filter,
                (filter1,filter2)->{
                    filter1.getExcludes().addAll(filter2.getExcludes());
                    return filter1;
                }
        ));
        return filterMap.values().toArray(new SimplePropertyPreFilter[filterMap.size()]);

    }

    /**
     * 过滤掉所有 ManyToOne、OneToOne、ManyToMany、OneToMany注解的属性
     * @param clazz
     * @return
     */
    public static SimplePropertyPreFilter getSimpleJsonFilter(Class clazz){
        SimplePropertyPreFilter filter=new SimplePropertyPreFilter(clazz);
        List<Field> fieldList = BeanUtil.getFieldList(clazz,
                new Class[]{ManyToMany.class, OneToMany.class, ManyToOne.class, OneToOne.class});
        fieldList.forEach(field -> {
            filter.getExcludes().add(field.getName());
        });
        return filter;
    }

    /**
     * 深度为0的过滤器,此过滤器的目的是只留下当前对象的简单属性以及带ManyToOne和OneToOne引用关系的属性
     * 根据当前对象类型：
     * 1、将当前对象类型中带ManyToMany和OneToMany注解的属性过滤；
     * 2、将当前对象类型中带ManyToOne和OneToOne的属性的获取,获取这些类型的简单属性过滤器
     *
     * remarks:如果遇到自关联属性、则忽略当前属性过滤器
     *
     * @param clazz
     * @return
     */
    public static SimplePropertyPreFilter[] getZeroDeepJsonFilter(Class clazz){
        List<SimplePropertyPreFilter> simplePropertyPreFilterList = new ArrayList<>();
        //1、获取当前类型所有带ManyToMany和OneToMany注解的属性集合
        List<Field> collectionFieldList = BeanUtil.getFieldList(clazz,new Class[]{ManyToMany.class, OneToMany.class});
        //2、获取当前类型所有带ManyToOne和OneToOne的注解的属性集合
        List<Field> objectFieldList = BeanUtil.getFieldList(clazz,new Class[]{ManyToOne.class, OneToOne.class});
        SimplePropertyPreFilter collectionFilter=new SimplePropertyPreFilter(clazz);
        //3.遍历所有带ManyToMany和OneToMany注解的属性集合,将属性名称放入过滤器
        collectionFieldList.forEach(collectionField -> {
            collectionFilter.getExcludes().add(collectionField.getName());
        });
        simplePropertyPreFilterList.add(collectionFilter);
        //4.遍历所有带ManyToOne和OneToOne的注解的属性集合
        objectFieldList.forEach(objectField -> {
            //4.1 得到该属性的对象类型
            Class fieldClazz = objectField.getType();
            //4.2 如果遇到自关联属性、则忽略当前属性过滤器
            if(fieldClazz==clazz){
                return;
            }
            //4.3 得到该属性的对象类型的简单属性过滤器
            simplePropertyPreFilterList.add(getSimpleJsonFilter(fieldClazz));
        });
        //5、合并多次调用的返回结果,将相同类的filter整合在一起
        Map<String,SimplePropertyPreFilter> filterMap= simplePropertyPreFilterList.stream().collect(Collectors.toMap(
                (filter)->filter.getClazz().getName(),
                (filter)->filter,
                (filter1,filter2)->{
                    filter1.getExcludes().addAll(filter2.getExcludes());
                    return filter1;
                }
        ));
        return filterMap.values().toArray(new SimplePropertyPreFilter[filterMap.size()]);
    }
}
