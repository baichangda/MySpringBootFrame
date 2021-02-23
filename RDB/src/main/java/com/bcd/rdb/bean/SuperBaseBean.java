package com.bcd.rdb.bean;


import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "主键(唯一标识符,自动生成)",accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "my")
    @GenericGenerator(name = "my", strategy = "com.bcd.rdb.jpa.MyIdentityGenerator")
    @Column(name = "id", nullable = false)
    public K id;
}
