package com.bcd.mongodb.test.bean;

import com.bcd.mongodb.bean.SuperBaseBean;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

@Accessors(chain = true)
@Getter
@Setter
@Document(collection = "departure")
//测试类
public class TestBean extends SuperBaseBean<String>{
    @ApiModelProperty(value = "班线code(长度20)")
    private String postlinecode;
    @ApiModelProperty(value = "班线名称(长度30)")
    private String postlinename;
}
