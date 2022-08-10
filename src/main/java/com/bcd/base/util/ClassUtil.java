package com.bcd.base.util;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@SuppressWarnings("unchecked")
public class ClassUtil {
    public static Type getParentUntil(Class startClass, Class... endClasses) {
        Type parentType = startClass.getGenericSuperclass();
        while (true) {
            if (parentType instanceof ParameterizedType) {
                Class rawType = (Class) ((ParameterizedType) parentType).getRawType();
                boolean isMatch = false;
                for (Class endClass : endClasses) {
                    if (rawType.equals(endClass)) {
                        isMatch = true;
                        break;
                    }
                }
                if (isMatch) {
                    break;
                } else {
                    parentType = rawType.getGenericSuperclass();
                }
            } else {
                parentType = ((Class) parentType).getGenericSuperclass();
            }
        }
        return parentType;
    }
}
