package com.base.util;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Administrator on 2017/1/9.
 */
@SuppressWarnings("unchecked")
public class BeanUtil {
    /**
     *  八大基础数据类型的原始类和封装类 和 Date
     */
    public final static Class[] BASE_DATA_TYPE=new Class[]{
            Integer.class,String.class,Double.class,Character.class,Byte.class,Float.class,
            Long.class,Short.class,Boolean.class,Date.class,
    int.class,byte.class,short.class,char.class,double.class,float.class,long.class,boolean.class};

    private final static Class[] JPA_PK_ANNOTATION=new Class[]{
            Id.class,EmbeddedId.class
    };

    /**
     * 找出对应clazz里面字段带 annotationClassArr 类型注解的字段集合
     * @param clazz
     * @param annotationClassArr
     * @return
     */
    public static List<Field> getFieldList(Class clazz,Class ... annotationClassArr){
        List<Field> returnList=new ArrayList<>();
        if(annotationClassArr==null||annotationClassArr.length==0){
            return returnList;
        }
        List<Field> fieldList= getAllFieldListByClass(clazz,null,null);
        fieldList.forEach(field -> {
            for(int i=0;i<=annotationClassArr.length-1;i++){
                Annotation annotation=field.getAnnotation(annotationClassArr[i]);
                if(annotation==null){
                    annotation=field.getDeclaredAnnotation(annotationClassArr[i]);
                }
                if(annotation!=null){
                    returnList.add(field);
                    return;
                }
            }
        });
        return returnList;
    }

    /**
     * 自动将sourceObj和targetObj相同属性的值进行注入，忽略不同属性，跳过值为空的属性
     * sourceObj字段必须有get方法，targetObj字段必须有set方法，否则不注入当前字段值
     * 逻辑为：
     * 1、先获取sourceObj所有字段
     * 2、根据字段名称获取匹配的get set方法
     * 3、执行get set方法赋值
     *
     * @param sourceObj 源对象
     * @param targetObj 目标对象
     */
    public static void autoInversionForCommonAttrIngoreNull(Object sourceObj, Object targetObj) {
        Class sourceClass = sourceObj.getClass();
        Class targetClass = targetObj.getClass();
        List<Method[]> list = getMappedMethodForInversion(sourceClass, targetClass, null,null);
        for (int i = 0; i <= list.size() - 1; i++) {
            try {
                Method[] methodArr = list.get(i);
                Object val=methodArr[0].invoke(sourceObj);
                if(val!=null){
                    methodArr[1].invoke(targetObj, val);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 自动将sourceObj和targetObj相同属性的值进行注入，忽略不同属性，跳过值为空的属性
     * sourceObj字段必须有get方法，targetObj字段必须有set方法，否则不注入当前字段值
     * 只进行基础属性的注入
     * 逻辑为：
     * 1、先获取sourceObj所有字段，判断数据类型
     * 2、根据字段名称获取匹配的get set方法
     * 3、执行get set方法赋值
     *
     * @param sourceObj 源对象
     * @param targetObj 目标对象
     */
    public static void autoInversionForBaseAttrIngoreNull(Object sourceObj, Object targetObj) {
        Class sourceClass = sourceObj.getClass();
        Class targetClass = targetObj.getClass();
        List<Method[]> list = getMappedMethodForInversion(sourceClass, targetClass, null,BASE_DATA_TYPE);
        for (int i = 0; i <= list.size() - 1; i++) {
            try {
                Method[] methodArr = list.get(i);
                Object val=methodArr[0].invoke(sourceObj);
                if(val!=null){
                    methodArr[1].invoke(targetObj, val);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 自动将sourceObj和targetObj相同属性的值进行注入，忽略不同属性
     * {判断一个属性是否赋值：
     *      1、targetObj和sourceObj都有这个属性
     *      2、targetObj中对应属性值为空,sourceObj中不为空
     * }
     * sourceObj字段必须有get方法，targetObj字段必须有set方法，否则不注入当前字段值
     * 只进行基础属性的注入
     * 逻辑为：
     * 1、先获取sourceObj所有字段，判断数据类型
     * 2、根据字段名称获取匹配的get set方法
     * 3、执行get set方法赋值
     *
     * @param sourceObj 源对象
     * @param targetObj 目标对象
     */
    public static void autoInversionForBaseAttrForNull(Object sourceObj, Object targetObj) {
        Class sourceClass = sourceObj.getClass();
        Class targetClass = targetObj.getClass();
        List<Method[][]> list = getMappedMethodArrForInversion(sourceClass, targetClass, null,BASE_DATA_TYPE);
        for (int i = 0; i <= list.size() - 1; i++) {
            try {
                Method[][] methodArr = list.get(i);
                Object sourceVal=methodArr[0][0].invoke(sourceObj);
                Object targetVal=methodArr[1][0].invoke(targetObj);
                if(targetVal==null&&sourceVal!=null){
                    methodArr[1][1].invoke(targetObj, sourceVal);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 自动将sourceObj和targetObj相同属性的值进行注入，忽略不同属性
     * sourceObj字段必须有get方法，targetObj字段必须有set方法，否则不注入当前字段值
     * 逻辑为：
     * 1、先获取sourceObj所有字段
     * 2、根据字段名称获取匹配的get set方法
     * 3、执行get set方法赋值
     *
     * @param sourceObj 源对象
     * @param targetObj 目标对象
     */
    public static void autoInversionForCommonAttr(Object sourceObj, Object targetObj) {
        autoInversionForCommonAttr(sourceObj, targetObj, null,null);
    }

    /**
     * 自动将sourceObj和targetObj相同属性的值进行注入，忽略不同属性
     * sourceObj字段必须有get方法，targetObj字段必须有set方法，否则不注入当前字段值
     * 逻辑为：
     * 1、先获取sourceObj所有字段
     * 2、根据字段名称获取匹配的get set方法
     * 3、执行get set方法赋值
     *
     * @param sourceObj 源对象
     * @param targetObj 目标对象
     * @param skipFieldArr 忽略注入的字段
     */
    public static void autoInversionForCommonAttr(Object sourceObj, Object targetObj, String[] skipFieldArr) {
        autoInversionForCommonAttr(sourceObj, targetObj, skipFieldArr,null);
    }

    /**
     * 自动将sourceObj和targetObj相同属性的值进行注入，忽略不同属性
     * sourceObj字段必须有get方法，targetObj字段必须有set方法，否则不注入当前字段值
     * 逻辑为：
     * 1、先获取sourceObj所有字段
     * 2、根据字段名称获取匹配的get set方法
     * 3、执行get set方法赋值
     *
     * @param sourceObj
     * @param targetObj
     * @param skipFieldArr 忽略注入的字段
     * @param classTypeArr 指定类型数组
     */
    public static void autoInversionForCommonAttr(Object sourceObj, Object targetObj, String[] skipFieldArr,Class[] classTypeArr) {
        Class sourceClass = sourceObj.getClass();
        Class targetClass = targetObj.getClass();
        List<Method[]> list = getMappedMethodForInversion(sourceClass, targetClass, skipFieldArr,classTypeArr);
        for (int i = 0; i <= list.size() - 1; i++) {
            try {
                Method[] methodArr = list.get(i);
                methodArr[1].invoke(targetObj, methodArr[0].invoke(sourceObj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取类的所有字段，包括public字段和本类中所有字段
     * 是getFields和getDeclaredFields并集
     *
     * @param clazz        类
     * @param skipFieldArr 忽略的字段数组
     * @param classTypeArr 指定类型数组
     * @return 所有字段集合
     */
    public static List<Field> getAllFieldListByClass(Class clazz, String[] skipFieldArr,Class[] classTypeArr) {
        List<Field> list = new ArrayList<Field>();
        Field[] fieldArr = clazz.getFields();
        Field[] declaredFieldArr = clazz.getDeclaredFields();

        Set<String> skipFieldSet = null;
        if (skipFieldArr != null) {
            skipFieldSet = new HashSet<String>();
            for (int i = 0; i <= skipFieldArr.length - 1; i++) {
                skipFieldSet.add(skipFieldArr[i]);
            }
        }

        Set<Class> classTypeSet = null;
        if (classTypeArr != null) {
            classTypeSet = new HashSet<Class>();
            for (int i = 0; i <= classTypeArr.length - 1; i++) {
                classTypeSet.add(classTypeArr[i]);
            }
        }

        for (int i = 0; i <= fieldArr.length - 1; i++) {
            //跳过字段
            if (skipFieldSet != null && skipFieldSet.contains(fieldArr[i].getName())) {
                continue;
            }
            //指定类型字段获取
            if(classTypeSet==null){
                fieldArr[i].setAccessible(true);
                list.add(fieldArr[i]);
            }else{
                if(classTypeSet.contains(fieldArr[i].getType())){
                    fieldArr[i].setAccessible(true);
                    list.add(fieldArr[i]);
                }
            }

        }
        for (int i = 0; i <= declaredFieldArr.length - 1; i++) {
            //跳过字段
            if (skipFieldSet != null && skipFieldSet.contains(declaredFieldArr[i].getName())) {
                continue;
            }
            //跳过public
            if (declaredFieldArr[i].getModifiers() == Modifier.PUBLIC) {
                continue;
            }
            //指定类型字段获取
            if(classTypeSet==null){
                declaredFieldArr[i].setAccessible(true);
                list.add(declaredFieldArr[i]);
            }else{
                if(classTypeSet.contains(declaredFieldArr[i].getType())){
                    declaredFieldArr[i].setAccessible(true);
                    list.add(declaredFieldArr[i]);
                }
            }
        }
        return list;
    }


    /**
     * 获取两个类匹配字段的 get set方法
     * sourceClass中字段get方法和targetClass中字段set方法
     *
     * @param sourceClass
     * @param targetClass
     * @param skipFieldArr 忽略的字段
     * @param classTypeArr 指定类型数组
     * @return
     */
    private static List<Method[]> getMappedMethodForInversion(Class sourceClass, Class targetClass, String[] skipFieldArr,Class[] classTypeArr) {
        List<Method[]> list = new ArrayList<Method[]>();
        List<Field> sourceFieldList = getAllFieldListByClass(sourceClass, skipFieldArr,classTypeArr);
        for (int i = 0; i <= sourceFieldList.size() - 1; i++) {
            Field field = sourceFieldList.get(i);
            String fieldName = sourceFieldList.get(i).getName();
            String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            Method setMethod = null;
            Method getMethod = null;

            try {
                getMethod = sourceClass.getMethod(getMethodName);
            } catch (NoSuchMethodException e) {
                try {
                    getMethod = sourceClass.getDeclaredMethod(getMethodName);
                } catch (NoSuchMethodException e1) {
                    continue;
                }
            }

            try {
                setMethod = targetClass.getMethod(setMethodName);
            } catch (NoSuchMethodException e) {
                try {
                    setMethod = targetClass.getDeclaredMethod(setMethodName, field.getType());
                } catch (NoSuchMethodException e1) {
                    continue;
                }
            }
            if (getMethod != null && setMethod != null) {
                getMethod.setAccessible(true);
                setMethod.setAccessible(true);
                list.add(new Method[]{getMethod, setMethod});
            }

        }
        return list;
    }


    /**
     * 获取两个类匹配字段的 get set方法数组
     * sourceClass中字段get set方法和targetClass中字段get set方法
     *
     * @param sourceClass
     * @param targetClass
     * @param skipFieldArr 忽略的字段
     * @param classTypeArr 指定类型数组
     * @return {{{get set},{get set}}....}
     */
    private static List<Method[][]> getMappedMethodArrForInversion(Class sourceClass, Class targetClass, String[] skipFieldArr,Class[] classTypeArr) {
        List<Method[][]> list = new ArrayList<Method[][]>();
        List<Field> sourceFieldList = getAllFieldListByClass(sourceClass, skipFieldArr,classTypeArr);
        for (int i = 0; i <= sourceFieldList.size() - 1; i++) {
            Field field = sourceFieldList.get(i);
            String fieldName = sourceFieldList.get(i).getName();
            String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            Method[] sourceMethodArr=null;
            Method[] targetMethodArr=null;

            try {
                Method getMethod = sourceClass.getMethod(getMethodName);
                Method setMethod= sourceClass.getMethod(setMethodName,field.getType());
                sourceMethodArr=new Method[]{getMethod,setMethod};

            } catch (NoSuchMethodException e) {
                try {
                    Method getMethod = sourceClass.getDeclaredMethod(getMethodName);
                    Method setMethod= sourceClass.getDeclaredMethod(setMethodName,field.getType());
                    sourceMethodArr=new Method[]{getMethod,setMethod};
                } catch (NoSuchMethodException e1) {
                    continue;
                }
            }

            try {
                Method getMethod = targetClass.getMethod(getMethodName);
                Method setMethod= targetClass.getMethod(setMethodName,field.getType());
                targetMethodArr=new Method[]{getMethod,setMethod};
            } catch (NoSuchMethodException e) {
                try {
                    Method getMethod = targetClass.getDeclaredMethod(getMethodName);
                    Method setMethod= targetClass.getDeclaredMethod(setMethodName,field.getType());
                    targetMethodArr=new Method[]{getMethod,setMethod};
                } catch (NoSuchMethodException e1) {
                    continue;
                }
            }
            if (sourceMethodArr != null && targetMethodArr != null) {
                sourceMethodArr[0].setAccessible(true);
                sourceMethodArr[1].setAccessible(true);
                targetMethodArr[0].setAccessible(true);
                targetMethodArr[1].setAccessible(true);
                list.add(new Method[][]{sourceMethodArr, targetMethodArr});
            }
        }
        return list;
    }

    /**
     * 将对象转换成为Map对象
     * 需要转换的属性必须 实现get方法
     *
     * @param obj
     * @param skipFieldArr 忽略的字段
     * @return
     */
    public static Map<String, Object> transferObjToMap(Object obj, String[] skipFieldArr) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Class clazz = obj.getClass();
        List<Field> fieldList = getAllFieldListByClass(clazz, skipFieldArr,null);

        for (int i = 0; i <= fieldList.size() - 1; i++) {
            Field field = fieldList.get(i);
            String fieldName = field.getName();
            Method getMethod = null;
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                getMethod = clazz.getMethod(getMethodName);
            } catch (NoSuchMethodException e) {
                try {
                    getMethod = clazz.getDeclaredMethod(getMethodName);
                } catch (NoSuchMethodException e1) {
                    continue;
                }
            }
            if (getMethod != null) {
                try {
                    map.put(fieldName, getMethod.invoke(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;

    }


    /**
     * 验证对象属性是否为空，为空则将数据库对象值赋值给要保存的对象
     *
     * @param source
     * @param desc
     * @param fieldNameArr
     */
    public void checkNullAndReplace(Object source, Object desc, String[] fieldNameArr) {
        if (source != null && desc != null && fieldNameArr != null && fieldNameArr.length > 0) {
            Class descClazz = desc.getClass();
            Class sourceClazz = source.getClass();
            for (int i = 0; i <= fieldNameArr.length - 1; i++) {
                try {
                    String fieldName = fieldNameArr[i];
                    String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Method getMethod = descClazz.getMethod(getMethodName);
                    if (getMethod.invoke(desc) == null) {
                        Method setMethod = descClazz.getMethod(setMethodName, getMethod.getReturnType());
                        Object val = sourceClazz.getMethod(getMethodName).invoke(source);
                        setMethod.invoke(desc, val);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }

    }


    /**
     * 根据JPA的Id注解获取主键值
     * @param obj
     * @return
     */
    public static Object getPKValByJPAAnnotation(Object obj) {

        Class clazz = obj.getClass();
        List<Field> fieldList = getAllFieldListByClass(clazz, null,BASE_DATA_TYPE);
        for (int i = 0; i <= fieldList.size() - 1; i++) {
            try {
                Field field = fieldList.get(i);
                Annotation annotation=null;
                for(int j=0;j<=JPA_PK_ANNOTATION.length-1;j++){
                    annotation=field.getAnnotation(JPA_PK_ANNOTATION[j]);
                    if(annotation==null){
                        annotation=field.getDeclaredAnnotation(JPA_PK_ANNOTATION[j]);
                    }
                    if(annotation!=null){
                        break;
                    }
                }
                if (annotation != null) {
                    String fieldName = field.getName();
                    String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    return clazz.getMethod(getMethodName).invoke(obj);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 获取对象的属性值,根据反射获取
     * @param obj
     * @param fieldName
     * @return
     */
    public static Object getFieldVal(Object obj,String fieldName){
        Class clazz=obj.getClass();
        String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method getMethod;
        try {
            getMethod=clazz.getMethod(getMethodName);
        } catch (NoSuchMethodException e) {
            try {
                getMethod=clazz.getDeclaredMethod(getMethodName);
            } catch (NoSuchMethodException e1) {
                return null;
            }
        }
        if(getMethod!=null) {
            try {
                return getMethod.invoke(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     *
     * 规则为：
     * 主键为null 或者 只在nodb中 则入 addList
     * 同时在db和nodb中 则入 updateList
     * 只在db中 则入 deleteList
     * @param dbCollection
     * @param collection
     * @param <T>
     * @return
     */
    public static <T>List<T>[] compareAndReturnCUD(Collection<T> dbCollection, Collection<T> collection){
        List<T> addList=new ArrayList<T>();
        List<T> updateList=new ArrayList<T>();
        List<T> deleteList=new ArrayList<T>();
        List<T> [] returnVal=new List[]{
                addList,updateList,deleteList
        };
        if((dbCollection==null||dbCollection.size()==0)&&
                (collection==null||collection.size()==0)){
            return returnVal;
        }else if(dbCollection==null||dbCollection.size()==0){
            addList.addAll(collection);
            return returnVal;
        }else if(collection==null||collection.size()==0){
            deleteList.addAll(dbCollection);
            return returnVal;
        }else{
            Iterator<T> it=collection.iterator();
            deleteList.addAll(dbCollection);
            A:while(it.hasNext()){
                T t=it.next();
                Object id=getPKValByJPAAnnotation(t);
                if(id==null){
                    addList.add(t);
                    continue;
                }
                Iterator<T> dbIt=dbCollection.iterator();
                while(dbIt.hasNext()){
                    T dbT=dbIt.next();
                    Object dbId=getPKValByJPAAnnotation(dbT);
                    if(id.equals(dbId)){
                        updateList.add(t);
                        Predicate<T> filter=(T e)->{
                            Object innerId= getPKValByJPAAnnotation(e);
                            return id.equals(innerId);
                        };
                        deleteList.removeIf(filter);
                        continue A;
                    }
                }
                //若不在db中
                addList.add(t);
            }
            return returnVal;
        }

    }

    /**
     *
     * @param obj1
     * @param obj2
     * @return
     */
    public static boolean checkIsEqual(Object obj1,Object obj2){
        if(obj1==obj2){
            return true;
        }
        if(obj1==null||obj2==null){
            return false;
        }
        if(obj1.getClass()!=obj2.getClass()){
            return false;
        }
        return obj1.equals(obj2);
    }
}
