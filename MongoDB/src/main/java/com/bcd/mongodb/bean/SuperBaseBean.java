package com.bcd.mongodb.bean;


import com.fasterxml.jackson.annotation.JsonFilter;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@JsonFilter("bcd")
public abstract class SuperBaseBean<K extends Serializable> implements Serializable {
    @ApiModelProperty(value = "主键(唯一标识符,自动生成)(不需要赋值)")
    @Id
    //主键
    public K id;

    public K getId() {
        return id;
    }

    public void setId(K id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id==null?0:id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj){
            return true;
        }else{
            if(obj==null){
                return false;
            }else{
                if(this.getClass()==obj.getClass()){
                    Object objId=((SuperBaseBean)obj).getId();
                    if(id==objId){
                        return true;
                    }else{
                        if(id==null||objId==null){
                            return false;
                        }else{
                            return id.equals(objId);
                        }
                    }
                }else{
                    return false;
                }
            }
        }
    }
}
