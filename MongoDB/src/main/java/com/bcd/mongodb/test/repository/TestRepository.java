package com.bcd.mongodb.test.repository;

import com.bcd.mongodb.repository.BaseRepository;
import com.bcd.mongodb.test.bean.TestBean;
import org.springframework.stereotype.Repository;


@Repository
public interface TestRepository extends BaseRepository<TestBean, String> {

}
