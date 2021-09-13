package com.bcd.sys.repository;


import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.sys.bean.UserBean;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends BaseRepository<UserBean, Long> {

}
