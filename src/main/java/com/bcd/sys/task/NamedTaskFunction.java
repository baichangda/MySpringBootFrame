package com.bcd.sys.task;


import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public abstract class NamedTaskFunction<T extends Task> implements TaskFunction<T> {
    private final static ConcurrentHashMap<String, NamedTaskFunction> storage = new ConcurrentHashMap<>();

    public NamedTaskFunction() {
        storage.put(getName(), this);
    }

    public static <T extends Task> NamedTaskFunction<T> from(String name) {
        return storage.get(name);
    }

    public abstract String getName();
}
