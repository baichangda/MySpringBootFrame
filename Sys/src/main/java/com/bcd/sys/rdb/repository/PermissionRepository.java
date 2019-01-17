package com.bcd.sys.rdb.repository;


import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.rdb.bean.PermissionBean;
import org.springframework.stereotype.Repository;


/**
 * 用户基础信息操作
 *
 * @author Aaric
 * @since 2017-04-26
 */
@Repository
public interface PermissionRepository extends BaseRepository<PermissionBean, Long> {

}
