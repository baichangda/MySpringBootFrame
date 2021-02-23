package com.bcd.mongodb.test.bean;

import com.bcd.mongodb.bean.SuperBaseBean;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class TestBean extends SuperBaseBean<String> {
    @Schema(description = "vin")
    private String vin;
    @Schema(description = "时间")
    private Date time;
}
