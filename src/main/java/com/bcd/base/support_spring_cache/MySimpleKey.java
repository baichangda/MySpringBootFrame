package com.bcd.base.support_spring_cache;

import lombok.Getter;
import com.bcd.base.util.JsonUtil;
import org.springframework.lang.Nullable;

import java.io.Serializable;

@Getter
public class MySimpleKey implements Serializable {
    private final String className;
    private final String methodName;
    private final Object[] args;
    private final String json;
    private final int hashCode;

    public MySimpleKey(String className, String methodName, Object... args) {
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        this.json = JsonUtil.toJson(new Object[]{className, methodName, args});
        this.hashCode=json.hashCode();
    }


    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other ||
                (other instanceof MySimpleKey && json.equals(((MySimpleKey) other).getJson())));
    }

    @Override
    public final int hashCode() {
        // Expose pre-calculated hashCode field
        return hashCode;
    }

    @Override
    public String toString() {
        return json;
    }
}
