package com.bcd.base.support_mongodb.test.bean;

import com.bcd.base.support_mongodb.bean.SuperBaseBean;
import com.bcd.base.support_mongodb.code.CodeGenerator;
import com.bcd.base.support_mongodb.code.CollectionConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Document(collection = "test")
//测试类
public class TestBean extends SuperBaseBean {
    @Schema(description = "vin")
    public String vin;
    @Schema(description = "时间")
    public Date time;
    public static void main(String[] args) {
        CollectionConfig config = new CollectionConfig("Test", "测试", TestBean.class);
        config.needCreateControllerFile = true;
        config.needCreateServiceFile = true;
        config.needValidateSaveParam = true;
        CodeGenerator.generate(config);
    }
}
