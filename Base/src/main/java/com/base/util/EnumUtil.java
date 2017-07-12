package com.base.util;

import com.base.condition.impl.StringCondition;
import com.base.em.bo.EnumItemService;
import com.base.em.bo.EnumTypeService;
import com.base.em.dto.EnumItemBean;
import com.base.em.dto.EnumTypeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by Administrator on 2017/5/26.
 */
@Component
public class EnumUtil {
    private static EnumItemService enumItemBO;
    private static EnumTypeService enumTypeBO;

    public EnumItemService getEnumItemBO() {
        return enumItemBO;
    }

    @Autowired
    public void setEnumItemBO(EnumItemService enumItemBO) {
        EnumUtil.enumItemBO = enumItemBO;
    }

    public EnumTypeService getEnumTypeBO() {
        return enumTypeBO;
    }
    @Autowired
    public void setEnumTypeBO(EnumTypeService enumTypeBO) {
        EnumUtil.enumTypeBO = enumTypeBO;
    }

    /**
     * @param code type的code
     * @return
     */
    public static Set<EnumItemBean> getEnumItemArr(String code){
        EnumTypeBean enumTypeDTO= enumTypeBO.findOne(new StringCondition("code",code, StringCondition.Handler.EQUAL));
        return enumTypeDTO.getEnumItemDTOSet();
    }

    /**
     *
     * @param code item的code
     * @return
     */
    public static EnumItemBean getEnumItem(String code){
        return enumItemBO.findOne(new StringCondition("code",code, StringCondition.Handler.EQUAL));
    }

    /**
     *
     * @param id item的id
     * @return
     */
    public static EnumItemBean getEnumItem(Long id){
        return enumItemBO.findOne(id);
    }
}
