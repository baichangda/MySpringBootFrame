package com.base.dto;


import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/2.
 */
@MappedSuperclass
public abstract class SuperBaseBean implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
