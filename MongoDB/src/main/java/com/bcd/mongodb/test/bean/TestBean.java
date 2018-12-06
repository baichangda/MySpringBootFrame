package com.bcd.mongodb.test.bean;

import com.bcd.mongodb.bean.SuperBaseBean;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "departure")
//测试类
public class TestBean extends SuperBaseBean<String>{
    //班线code
    private String postlinecode;
    //班线名称
    private String postlinename;

    public String getPostlinecode() {
        return postlinecode;
    }

    public void setPostlinecode(String postlinecode) {
        this.postlinecode = postlinecode;
    }

    public String getPostlinename() {
        return postlinename;
    }

    public void setPostlinename(String postlinename) {
        this.postlinename = postlinename;
    }
}
