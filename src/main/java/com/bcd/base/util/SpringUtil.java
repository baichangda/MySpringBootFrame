package com.bcd.base.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SpringUtil {

    private final static String SPRING_PROPERTIES_PATH = System.getProperty("user.dir") + "/src/main/resources/application.yml";

    public static JsonNode[] getSpringProps(String... keys) throws IOException {
        YAMLMapper yamlMapper = YAMLMapper.builder().build();
        final JsonNode base = yamlMapper.readTree(new File(SPRING_PROPERTIES_PATH));
        final JsonNode suffix = Optional.ofNullable(base.get("spring")).map(e -> e.get("profiles")).map(e -> e.get("active")).orElse(null);
        JsonNode active = null;
        if (suffix != null) {
            String activePathStr = SPRING_PROPERTIES_PATH.substring(0, SPRING_PROPERTIES_PATH.lastIndexOf('.')) + "-" + suffix.asText() + "." + SPRING_PROPERTIES_PATH.substring(SPRING_PROPERTIES_PATH.indexOf('.') + 1);
            active = yamlMapper.readTree(new File(activePathStr));
        }
        JsonNode[] res = new JsonNode[keys.length];
        A:
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String[] arr = key.split("\\.");
            JsonNode temp = active;
            if (active != null) {
                for (String s : arr) {
                    temp = temp.get(s);
                    if (temp == null) {
                        break;
                    }
                }
            }
            if (temp == null) {
                temp = base;
                for (String s : arr) {
                    temp = temp.get(s);
                    if (temp == null) {
                        continue A;
                    }
                }
            }
            res[i] = temp;
        }
        return res;
    }
}
