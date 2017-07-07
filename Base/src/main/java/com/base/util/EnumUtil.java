package com.base.util;

import com.base.condition.impl.StringCondition;
import com.base.em.bo.EnumItemBO;
import com.base.em.bo.EnumTypeBO;
import com.base.em.dto.EnumItemDTO;
import com.base.em.dto.EnumTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by Administrator on 2017/5/26.
 */
@Component
public class EnumUtil {
    private static EnumItemBO enumItemBO;
    private static EnumTypeBO enumTypeBO;

    public EnumItemBO getEnumItemBO() {
        return enumItemBO;
    }

    @Autowired
    public void setEnumItemBO(EnumItemBO enumItemBO) {
        EnumUtil.enumItemBO = enumItemBO;
    }

    public EnumTypeBO getEnumTypeBO() {
        return enumTypeBO;
    }
    @Autowired
    public void setEnumTypeBO(EnumTypeBO enumTypeBO) {
        EnumUtil.enumTypeBO = enumTypeBO;
    }

    /**
     * @param code type的code
     * @return
     */
    public static Set<EnumItemDTO> getEnumItemArr(String code){
        EnumTypeDTO enumTypeDTO= enumTypeBO.findOne(new StringCondition("code",code, StringCondition.Handler.EQUAL));
        return enumTypeDTO.getEnumItemDTOSet();
    }

    /**
     *
     * @param code item的code
     * @return
     */
    public static EnumItemDTO getEnumItem(String code){
        return enumItemBO.findOne(new StringCondition("code",code, StringCondition.Handler.EQUAL));
    }

    /**
     *
     * @param id item的id
     * @return
     */
    public static EnumItemDTO getEnumItem(Long id){
        return enumItemBO.findOne(id);
    }
}
