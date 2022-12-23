package com.bcd.base.support_jpa.identity;

import com.bcd.base.support_jpa.bean.SuperBaseBean;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentityGenerator;

import java.io.Serializable;

/**
 * 支持自定义主键值
 */
public class MyIdentityGenerator extends IdentityGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor s, Object obj) {
        if (obj instanceof SuperBaseBean) {
            Serializable id = ((SuperBaseBean) obj).id;
            if (id == null) {
                return super.generate(s, obj);
            } else {
                return id;
            }
        } else {
            return super.generate(s, obj);
        }
    }
}
