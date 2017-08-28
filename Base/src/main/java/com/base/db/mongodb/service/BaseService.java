package com.base.db.mongodb.service;

import com.base.db.mongodb.repository.MongoBaseRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/8/25.
 */
public class BaseService<T,K extends Serializable>{
    public MongoBaseRepository<T,K> repository;

    public List<T> findAll(){
        return repository.findAll();
    }


}
