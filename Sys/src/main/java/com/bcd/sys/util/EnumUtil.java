package com.bcd.sys.util;

import com.bcd.base.condition.impl.StringCondition;
import com.bcd.sys.bean.EnumItemBean;
import com.bcd.sys.bean.EnumTypeBean;
import com.bcd.sys.service.EnumItemService;
import com.bcd.sys.service.EnumTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;


/**
 * Created by Administrator on 2017/5/26.
 */
@Component
public class EnumUtil {
    private static EnumItemService enumItemService;
    private static EnumTypeService enumTypeService;

    @Autowired
    public void setEnumItemService(EnumItemService enumItemService) {
        EnumUtil.enumItemService = enumItemService;
    }

    @Autowired
    public void setEnumTypeService(EnumTypeService enumTypeService) {
        EnumUtil.enumTypeService = enumTypeService;
    }

    /**
     * @param code type的code
     * @return
     */
    public static Set<EnumItemBean> getEnumItemSet(String code){
        EnumTypeBean enumTypeBean= enumTypeService.findOne(new StringCondition("code",code, StringCondition.Handler.EQUAL));
        return enumTypeBean.getEnumItemBeanSet();
    }

    /**
     *
     * @param code item的code
     * @return
     */
    public static EnumItemBean getEnumItem(String code){
        return enumItemService.findOne(new StringCondition("code",code, StringCondition.Handler.EQUAL));
    }

    /**
     *
     * @param id item的id
     * @return
     */
    public static EnumItemBean getEnumItem(Long id){
        return enumItemService.findOne(id);
    }
}
