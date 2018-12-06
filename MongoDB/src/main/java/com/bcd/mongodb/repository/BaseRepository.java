package com.bcd.mongodb.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/25.
 */
@NoRepositoryBean
public interface BaseRepository<T,K extends Serializable> extends MongoRepository<T,K> {

}
