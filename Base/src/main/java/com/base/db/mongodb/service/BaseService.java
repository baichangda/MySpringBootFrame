package com.base.db.mongodb.service;

import com.base.db.mongodb.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/8/25.
 */
public class BaseService<T,K extends Serializable>{
    @Autowired
    public BaseRepository<T,K> repository;

    public List<T> findAll(){
        return repository.findAll();
    }


}
