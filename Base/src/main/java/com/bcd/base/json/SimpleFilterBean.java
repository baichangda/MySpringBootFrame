package com.bcd.base.json;

import java.util.HashSet;
import java.util.Set;

public class SimpleFilterBean {
    private Class clazz;
    private final Set<String> excludes =new HashSet<>();
    private final Set<String> includes =new HashSet<>();

    public SimpleFilterBean(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public Set<String> getIncludes() {
        return includes;
    }

}
