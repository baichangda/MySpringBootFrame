package com.bcd.ics.json;

import com.bcd.base.exception.BaseRuntimeException;
import com.bcd.base.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.incarcloud.skeleton.anno.ICSComponent;

@ICSComponent
public class JsonReader implements com.incarcloud.skeleton.json.JsonReader{
    @Override
    public String toJson(Object o) {
        try {
            return JsonUtil.GLOBAL_OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw BaseRuntimeException.getException(e);
        }
    }
}
