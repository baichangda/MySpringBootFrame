package com.base.em.dto;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.base.dto.AbstractBaseDTO;

import javax.persistence.*;

/**
 * Created by Administrator on 2017/5/18.
 */
@Entity
@Table(name = "t_enum_item")
public class EnumItemDTO extends AbstractBaseDTO {

    private Long typeId;
    private String name;
    private String code;
    private String remark;

    @ManyToOne
    @JoinColumn(name="typeId",insertable = false,updatable = false)
    private EnumTypeDTO enumTypeDTO;

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

    public void setEnumTypeDTO(EnumTypeDTO enumTypeDTO) {
        this.enumTypeDTO = enumTypeDTO;
    }

    public EnumTypeDTO getEnumTypeDTO() {
        return enumTypeDTO;
    }

    public static SimplePropertyPreFilter getSimpleJsonFilter(){
        SimplePropertyPreFilter simplePropertyPreFilter=new SimplePropertyPreFilter(EnumItemDTO.class);
        simplePropertyPreFilter.getExcludes().add("enumTypeDTO");
        return simplePropertyPreFilter;
    }

    public static SimplePropertyPreFilter[] getOneDeepJsonFilter(){
        return new SimplePropertyPreFilter[]{
                EnumTypeDTO.getSimpleJsonFilter()
        };
    }
}
