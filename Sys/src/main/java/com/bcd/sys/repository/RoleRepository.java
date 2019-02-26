package com.bcd.sys.rdb.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.rdb.bean.RoleBean;
import org.springframework.stereotype.Repository;


/**
 * 角色操作
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Repository
public interface RoleRepository extends BaseRepository<RoleBean, Long> {

}
