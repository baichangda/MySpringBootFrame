package com.bcd.mongodb.test.bean;

import com.bcd.mongodb.bean.SuperBaseBean;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Accessors(chain = true)
@Getter
@Setter
@Document(collection = "test")
//测试类
public class TestBean extends SuperBaseBean<String>{
    @ApiModelProperty(value = "vin")
    private String vin;
    @ApiModelProperty(value = "时间")
    private Date time;
}
