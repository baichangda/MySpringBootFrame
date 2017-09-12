package com.sys.bean;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.bcd.rdb.bean.SuperBaseBean;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Administrator on 2017/5/18.
 */
@Entity
@Table(name = "t_enum_item")
public class EnumItemBean extends SuperBaseBean {

    private Long typeId;
    private String name;
    private String code;
    private String remark;

    @ManyToOne
    @JoinColumn(name="typeId",insertable = false,updatable = false)
    private EnumTypeBean enumTypeDTO;

    public String getName() {
        return name;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
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

    public void setEnumTypeDTO(EnumTypeBean enumTypeDTO) {
        this.enumTypeDTO = enumTypeDTO;
    }

    public EnumTypeBean getEnumTypeDTO() {
        return enumTypeDTO;
    }

    public static SimplePropertyPreFilter getSimpleJsonFilter(){
        SimplePropertyPreFilter simplePropertyPreFilter=new SimplePropertyPreFilter(EnumItemBean.class);
        simplePropertyPreFilter.getExcludes().add("enumTypeDTO");
        return simplePropertyPreFilter;
    }

    public static SimplePropertyPreFilter[] getOneDeepJsonFilter(){
        return new SimplePropertyPreFilter[]{
                EnumTypeBean.getSimpleJsonFilter()
        };
    }
}
