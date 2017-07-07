package com.base.em.dto;

import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.base.dto.AbstractBaseDTO;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/18.
 */
@Entity
@Table(name = "t_enum_type")
public class EnumTypeDTO extends AbstractBaseDTO{
    private String name;
    private String code;
    private String remark;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="typeId")
    private Set<EnumItemDTO> enumItemDTOSet=new HashSet<>();

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

    public Set<EnumItemDTO> getEnumItemDTOSet() {
        return enumItemDTOSet;
    }

    public void setEnumItemDTOSet(Set<EnumItemDTO> enumItemDTOSet) {
        this.enumItemDTOSet = enumItemDTOSet;
    }

    public static SimplePropertyPreFilter getSimpleJsonFilter(){
        SimplePropertyPreFilter simplePropertyPreFilter=new SimplePropertyPreFilter(EnumTypeDTO.class);
        simplePropertyPreFilter.getExcludes().add("enumItemDTOSet");
        return simplePropertyPreFilter;
    }

    public static SimplePropertyPreFilter[] getOneDeepJsonFilter(){
        return new SimplePropertyPreFilter[]{
                EnumItemDTO.getSimpleJsonFilter()
        };
    }
}
