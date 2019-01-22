package com.bcd.base.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.HashMap;
import java.util.Map;

public class XmlUtil {
    public final static XmlMapper GLOBAL_XML_MAPPER= withConfig(new XmlMapper());

    public static XmlMapper withConfig(XmlMapper xmlMapper){
        //1、应用json过滤器配置
        JsonUtil.withConfig(xmlMapper);
        return xmlMapper;
    }

    public static void main(String [] args) throws JsonProcessingException {
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put("A","a");
        dataMap.put("B","b");
        dataMap.put("C",new HashMap<String,String>(){{
            put("D","d");
        }});
        String res=XmlUtil.GLOBAL_XML_MAPPER.writeValueAsString(dataMap);

        System.out.println("\nRes: "+res);
    }
}
