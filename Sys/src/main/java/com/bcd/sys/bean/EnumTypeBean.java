package com.bcd.sys.bean;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.bcd.rdb.bean.SuperBaseBean;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/18.
 */
@Entity
@Table(name = "t_enum_type")
public class EnumTypeBean extends SuperBaseBean {
    private String name;
    private String code;
    private String remark;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="typeId")
    private Set<EnumItemBean> enumItemDTOSet=new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Set<EnumItemBean> getEnumItemDTOSet() {
        return enumItemDTOSet;
    }

    public void setEnumItemDTOSet(Set<EnumItemBean> enumItemDTOSet) {
        this.enumItemDTOSet = enumItemDTOSet;
    }

    public static SimplePropertyPreFilter getSimpleJsonFilter(){
        SimplePropertyPreFilter simplePropertyPreFilter=new SimplePropertyPreFilter(EnumTypeBean.class);
        simplePropertyPreFilter.getExcludes().add("enumItemDTOSet");
        return simplePropertyPreFilter;
    }

    public static SimplePropertyPreFilter[] getOneDeepJsonFilter(){
        return new SimplePropertyPreFilter[]{
                EnumItemBean.getSimpleJsonFilter()
        };
    }
}
