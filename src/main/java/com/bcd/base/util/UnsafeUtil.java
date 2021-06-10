package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeUtil {

    public static Unsafe getUnsafe(){
        return Singleton.INSTANCE;
    }

    private final static class Singleton{
        private final static Unsafe INSTANCE = getInstance();
        private static Unsafe getInstance() {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                return (Unsafe) field.get(null);
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
                throw BaseRuntimeException.getException(e);
            }
        }
    }


    public static long fieldOffset(Field field){
        if(Modifier.isStatic(field.getModifiers())){
            return getUnsafe().staticFieldOffset(field);
        }else{
            return getUnsafe().objectFieldOffset(field);
        }
    }



    public static int fieldType(Field field) {
        Class<?> fieldType = field.getType();
        if (fieldType == byte.class) {
            return 1;
        } else if (fieldType == short.class) {
            return 2;
        } else if (fieldType == int.class) {
            return 3;
        } else if (fieldType == long.class) {
            return 4;
        } else if (fieldType == float.class) {
            return 5;
        } else if (fieldType == double.class) {
            return 6;
        } else if (fieldType == char.class) {
            return 7;
        } else if (fieldType == boolean.class) {
            return 8;
        } else {
            return 0;
        }
    }

    /**
     * unsafe方法获取值
     *
     * @param instance
     * @param offset
     * @param type
     * @return
     */
    public static Object getValue(Object instance, long offset, int type) {
        switch (type) {
            case 1: {
                return getUnsafe().getByte(instance, offset);
            }
            case 2: {
                return getUnsafe().getShort(instance, offset);
            }
            case 3: {
                return getUnsafe().getInt(instance, offset);
            }
            case 4: {
                return getUnsafe().getLong(instance, offset);
            }
            case 5: {
                return getUnsafe().getFloat(instance, offset);
            }
            case 6: {
                return getUnsafe().getDouble(instance, offset);
            }
            case 7: {
                return getUnsafe().getChar(instance, offset);
            }
            case 8: {
                return getUnsafe().getBoolean(instance, offset);
            }
            default: {
                return getUnsafe().getObject(instance, offset);
            }
        }
    }

    /**
     * unsafe设置值、替代反射
     *
     * @param instance
     * @param val
     * @param offset
     * @param type
     */
    public static void setValue(Object instance, Object val, long offset, int type) {
        switch (type) {
            case 1: {
                getUnsafe().putByte(instance, offset, (byte) val);
                return;
            }
            case 2: {
                getUnsafe().putShort(instance, offset, (short) val);
                return;
            }
            case 3: {
                getUnsafe().putInt(instance, offset, (int) val);
                return;
            }
            case 4: {
                getUnsafe().putLong(instance, offset, (long) val);
                return;
            }
            case 5: {
                getUnsafe().putFloat(instance, offset, (float) val);
                return;
            }
            case 6: {
                getUnsafe().putDouble(instance, offset, (double) val);
                return;
            }
            case 7: {
                getUnsafe().putChar(instance, offset, (char) val);
                return;
            }
            case 8: {
                getUnsafe().putBoolean(instance, offset, (boolean) val);
                return;
            }
            default: {
                getUnsafe().putObject(instance, offset, val);
            }
        }

    }
}
