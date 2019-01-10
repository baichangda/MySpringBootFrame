package com.bcd.base.json.jackson.filter;

import com.bcd.base.json.SimpleFilterBean;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;

import java.util.Collection;
import java.util.Set;

@SuppressWarnings("unchecked")
public class SimpleJacksonFilter implements PropertyFilter{
    private SimpleFilterBean[] filterBeans;

    public SimpleJacksonFilter(SimpleFilterBean ... beans) {
        this.filterBeans=beans;
    }

    public SimpleJacksonFilter(Collection<SimpleFilterBean> beans){
        if(beans!=null&&!beans.isEmpty()){
            this.filterBeans=beans.stream().toArray(len->new SimpleFilterBean[len]);
        }
    }

    public boolean isSerializeAsField(Object pojo, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer) {
        if(pojo!=null&&filterBeans!=null&&filterBeans.length>0){
            for (SimpleFilterBean filterBean : filterBeans) {
                Class clazz = filterBean.getClazz();
                Set<String> includes = filterBean.getIncludes();
                Set<String> excludes = filterBean.getExcludes();
                if (clazz == null || clazz.isAssignableFrom(pojo.getClass())) {
                    if (!includes.isEmpty()) {
                        if (!includes.contains(writer.getName())) {
                            return false;
                        }
                    } else {
                        if (!excludes.isEmpty()&&excludes.contains(writer.getName())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void serializeAsField(Object pojo, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer) throws Exception {
        boolean isSerialize=isSerializeAsField(pojo, gen, prov, writer);
        if(isSerialize){
            writer.serializeAsField(pojo, gen, prov);
        }else if(!gen.canOmitFields()){
            writer.serializeAsOmittedField(pojo, gen, prov);
        }
    }

    @Override
    public void serializeAsElement(Object elementValue, JsonGenerator gen, SerializerProvider prov, PropertyWriter writer) throws Exception {
        writer.serializeAsElement(elementValue, gen, prov);
    }

    /** @deprecated */
    @Override
    @Deprecated
    public void depositSchemaProperty(PropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) throws JsonMappingException {
        writer.depositSchemaProperty(propertiesNode, provider);
    }

    @Override
    public void depositSchemaProperty(PropertyWriter writer, JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) throws JsonMappingException {
        writer.depositSchemaProperty(objectVisitor, provider);
    }
}
