package com.bcd.base.support_mongodb.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoDBConfig implements InitializingBean {

    // 参考：https://stackoverflow.com/questions/23517977/spring-boot-mongodb-how-to-remove-the-class-column
    @Autowired
    @Lazy
    private MappingMongoConverter mappingMongoConverter;

    @Override
    public void afterPropertiesSet() {
        //去除mongodb对象中的_class属性
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}