package com.bcd.base.support_mongodb.test.repository;

import com.bcd.base.support_mongodb.repository.BaseRepository;
import com.bcd.base.support_mongodb.test.bean.TestBean;
import org.springframework.stereotype.Repository;


@Repository
public interface TestRepository extends BaseRepository<TestBean, String> {

}
