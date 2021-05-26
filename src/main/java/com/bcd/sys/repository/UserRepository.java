package com.bcd.sys.repository;


import com.bcd.base.support_rdb.repository.BaseRepository;
import com.bcd.sys.bean.UserBean;
import org.springframework.stereotype.Repository;


/**
 * 用户基础信息操作
 *
 * @author Aaric
 * @since 2017-04-26
 */
@Repository
public interface UserRepository extends BaseRepository<UserBean, Long> {

}
