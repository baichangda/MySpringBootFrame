package com.base.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;


/**
 * Created by Administrator on 2017/4/11.
 */
@NoRepositoryBean
public interface BaseRepository<T,K extends Serializable> extends JpaRepository<T,K>,JpaSpecificationExecutor<T> {

}
