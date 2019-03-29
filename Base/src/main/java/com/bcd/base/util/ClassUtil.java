package com.bcd.base.util;


import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;

public class ClassUtil {
    public static Type getParentUntil(Class startClass,Class ... endClasses){
        Type parentType=startClass.getGenericSuperclass();
        while(true){
            if(parentType instanceof ParameterizedTypeImpl){
                Class rawType= ((ParameterizedTypeImpl) parentType).getRawType();
                boolean isMatch=false;
                for (Class endClass : endClasses) {
                    if(rawType.equals(endClass)){
                        isMatch=true;
                        break;
                    }
                }
                if(isMatch){
                    break;
                }else{
                    parentType=rawType.getGenericSuperclass();
                }
            }else{
                parentType=((Class)parentType).getGenericSuperclass();
            }
        }
        return parentType;
    }
}
