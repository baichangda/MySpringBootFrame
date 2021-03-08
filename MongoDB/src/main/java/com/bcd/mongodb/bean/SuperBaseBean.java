package com.bcd.mongodb.bean;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@Accessors(chain = true)
@Getter
@Setter
public abstract class SuperBaseBean<K extends Serializable> implements Serializable {
    @Schema(description = "主键(唯一标识符,自动生成)", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    //主键
    public K id;

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            if (obj == null) {
                return false;
            } else {
                if (this.getClass() == obj.getClass()) {
                    Object objId = ((SuperBaseBean) obj).getId();
                    if (id == objId) {
                        return true;
                    } else {
                        if (id == null || objId == null) {
                            return false;
                        } else {
                            return id.equals(objId);
                        }
                    }
                } else {
                    return false;
                }
            }
        }
    }
}
