package com.bcd.base.support_jdbc.bean;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@Getter
@Setter
public abstract class SuperBaseBean implements Serializable {
    @Schema(description = "主键(唯一标识符,自动生成)", accessMode = Schema.AccessMode.READ_ONLY)
    //主键
    public Long id;

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
                if (obj instanceof SuperBaseBean) {
                    Object objId = ((SuperBaseBean) obj).id;
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
