package com.bcd.base.mongodb.test.repository;

import com.bcd.base.mongodb.repository.BaseRepository;
import com.bcd.base.mongodb.test.bean.TestBean;
import org.springframework.stereotype.Repository;


@Repository
public interface TestRepository extends BaseRepository<TestBean, String> {

}
