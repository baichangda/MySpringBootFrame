package com.bcd.base.rdb.jpa;

import com.bcd.base.rdb.bean.SuperBaseBean;
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
            Serializable id = ((SuperBaseBean) obj).getId();
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
