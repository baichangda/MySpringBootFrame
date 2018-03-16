package com.bcd.config.aliyun.properties;

public class SmsProperties {
    //短信API产品名称（短信产品名固定，无需修改） Dysmsapi
    public String product;
    //短信API产品域名（接口地址固定，无需修改） dysmsapi.aliyuncs.com
    public String domain;
    //暂时不支持多region（请勿修改） cn-hangzhou
    public String regionId;

    public String endpointName;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }
}
