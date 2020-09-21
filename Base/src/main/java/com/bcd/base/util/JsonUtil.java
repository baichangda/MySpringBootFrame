package com.bcd.base.util;

import com.bcd.base.exception.BaseRuntimeException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Administrator on 2017/5/12.
 */
@SuppressWarnings("unchecked")
public class JsonUtil {
    //此空过滤器必须定义在全局 GLOBAL_OBJECT_MAPPER 之前
    public final static ObjectMapper GLOBAL_OBJECT_MAPPER = withConfig(new ObjectMapper());


    public static JavaType getJavaType(Type type) {
        //1、判断是否带有泛型
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            //1.1、获取泛型类型
            Class rawClass = (Class) ((ParameterizedType) type).getRawType();
            JavaType[] javaTypes = new JavaType[actualTypeArguments.length];
            for (int i = 0; i < actualTypeArguments.length; i++) {
                //1.2、泛型也可能带有泛型，递归获取
                javaTypes[i] = getJavaType(actualTypeArguments[i]);
            }
            return TypeFactory.defaultInstance().constructParametricType(rawClass, javaTypes);
        } else {
            //2、简单类型直接用该类构建JavaType
            Class cla = (Class) type;
            return TypeFactory.defaultInstance().constructParametricType(cla, new JavaType[0]);
        }
    }


    /**
     * 为ObjectMapper重新设置MapSerializer,使其能使用PropertyFilter过滤属性,并为所有的Map添加过滤器,如果设置了map过滤,则必须为objectMapper设置默认过滤器(默认设置空的过滤器)
     * 设置所有Number属性的 输出为字符串(Long类型数字传入前端会进行四舍五入导致精度丢失,为了避免这种情况,所有的数字全部采用String格式化)
     * 设置忽略null属性输出
     * 设置在解析json字符串为实体类时候,忽略多余的属性
     *
     * 不会生成新的ObjectMapper,只会改变当前传入的ObjectMapper
     *
     * @param t
     * @return
     */
    public static <T extends ObjectMapper> T withConfig(T t) {
        //1、设置map过滤器
        SimpleModule simpleModule = new SimpleModule();
        //2、设置所有Number属性的 输出为字符串
        simpleModule.addSerializer(Number.class, ToStringSerializer.instance);
        t.registerModule(simpleModule);
        //3、设置忽略null属性输出
        t.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //4、设置在解析json字符串为实体类时候,忽略多余的属性
        t.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //5、设置在序列化时候遇到空属性对象时候,不抛出异常
        t.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return t;
    }

    /**
     * 此方法会调用 withConfig 改变objectMapper
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        try {
            return GLOBAL_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw BaseRuntimeException.getException(e);
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        int i=1;
        System.out.println((++i)+(++i));
        TestBean t1=new TestBean();
        t1.setId(1);
        t1.setName("t1");
        TestBean t2=new TestBean();
        t2.setId(2);
        t2.setName("t2");
        TestBean t3=new TestBean();
        t2.setId(3);
        t2.setName("t3");
        t1.setDataList(Arrays.asList(t2,t3));
        ObjectMapper objectMapper=JsonUtil.withConfig(new ObjectMapper());
        SimpleModule simpleModule=new SimpleModule();
        simpleModule.addDeserializer(TestBean.class, new StdDeserializer<TestBean>(TestBean.class) {
            @Override
            public TestBean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                String field=p.nextFieldName();
                JsonToken token= p.nextToken();
                return null;
            }
        });
        objectMapper.registerModule(simpleModule);
        TestBean res= objectMapper.readValue(JsonUtil.toJson(t1),TestBean.class);
    }

}
class TestBean{
    private Integer id;
    private String name;
    private List<TestBean> dataList;
    private String dataJson;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestBean> getDataList() {
        return dataList;
    }

    public void setDataList(List<TestBean> dataList) {
        this.dataList = dataList;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }
}
