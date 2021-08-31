package com.bcd.sys.repository;

import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.sys.bean.PermissionBean;
import org.springframework.stereotype.Repository;


@Repository
public interface PermissionRepository extends BaseRepository<PermissionBean, Long> {

}
