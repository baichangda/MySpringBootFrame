package com.bcd.api.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiParamData {
    private String name;
    private String desc;
    private boolean required;

    public ApiParamData(String name, String desc, boolean required) {
        this.name = name;
        this.desc = desc;
        this.required = required;
    }

    public String toString() {
        return name + " : " + desc + (required ? "(必填)" : "(非必填)");
    }

}
