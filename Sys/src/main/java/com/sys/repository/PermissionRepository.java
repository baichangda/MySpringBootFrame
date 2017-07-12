package com.sys.repository;


import com.base.repository.BaseRepository;
import com.sys.bean.PermissionBean;
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
