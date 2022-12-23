package com.bcd.base.support_mongodb.test.bean;

import com.bcd.base.support_mongodb.bean.SuperBaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "test")
//测试类
public class TestBean extends SuperBaseBean<String> {
    @Schema(description = "vin")
    public String vin;
    @Schema(description = "时间")
    public Date time;
}
