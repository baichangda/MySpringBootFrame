package com.bcd.sys.repository;

import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.sys.bean.RoleBean;
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
