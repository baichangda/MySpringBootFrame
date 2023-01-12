package com.bcd.base.support_mongodb.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;


/**
 * Created by Administrator on 2017/8/25.
 */
@NoRepositoryBean
public interface BaseRepository<T> extends MongoRepository<T, String> {

}
