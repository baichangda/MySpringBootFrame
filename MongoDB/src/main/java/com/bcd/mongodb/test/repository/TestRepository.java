package com.bcd.mongodb.test.repository;

import com.bcd.mongodb.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import com.bcd.mongodb.test.bean.TestBean;

@Repository
public interface TestRepository extends BaseRepository<TestBean, String> {

}
