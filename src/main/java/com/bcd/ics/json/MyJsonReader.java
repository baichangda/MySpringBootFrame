package com.bcd.ics.json;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.incarcloud.skeleton.anno.ICSComponent;
import com.incarcloud.skeleton.json.JsonReader;

//必须加入此注解,才会扫描到
@ICSComponent
public class MyJsonReader implements JsonReader{
    @Override
    public String toJson(Object o) {
        try {
            return JsonUtil.GLOBAL_OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
