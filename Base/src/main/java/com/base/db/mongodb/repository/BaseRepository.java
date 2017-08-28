package com.base.db.mongodb.repository;


import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/25.
 */
public interface BaseRepository<T,K extends Serializable> extends MongoRepository<T,K> {

}
