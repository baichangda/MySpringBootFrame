package com.sys.repository;


import com.base.repository.BaseRepository;
import com.sys.bean.UserBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户基础信息操作
 *
 * @author Aaric
 * @since 2017-04-26
 */
@Repository
public interface UserRepository extends BaseRepository<UserBean, Long> {

}
