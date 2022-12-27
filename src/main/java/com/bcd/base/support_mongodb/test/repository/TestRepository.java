package com.bcd.base.support_mongodb.test.repository;

import com.bcd.base.support_mongodb.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import com.bcd.base.support_mongodb.test.bean.TestBean;

@Repository
public interface TestRepository extends BaseRepository<TestBean> {

}
