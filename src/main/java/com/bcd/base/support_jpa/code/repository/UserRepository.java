package com.bcd.base.support_jpa.code.repository;

import com.bcd.base.support_jpa.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import com.bcd.base.support_jpa.code.bean.UserBean;

@Repository
public interface UserRepository extends BaseRepository<UserBean, Long> {

}
