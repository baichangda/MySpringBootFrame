package com.base.db.mongodb.bean;


import com.base.util.BeanUtil;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@MappedSuperclass
public abstract class SuperBaseBean implements Serializable {
    @Id
    public Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id==null?0:id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return BeanUtil.checkIsEqual(this,obj);
    }
}
