package com.bcd.rdb.bean;



import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@Accessors(chain = true)
@Getter
@Setter
@MappedSuperclass
@SuppressWarnings("unchecked")
public abstract class SuperBaseBean<K extends Serializable> implements Serializable {
    @ApiModelProperty(value = "主键(唯一标识符,自动生成)(不需要赋值)")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "my")
    @GenericGenerator(name="my",strategy = "com.bcd.rdb.jpa.MyIdentityGenerator")
    @Column(name = "id", nullable = false)
    public K id;
}
