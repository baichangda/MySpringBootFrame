package com.bcd.sys.repository;

import com.bcd.rdb.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import com.bcd.sys.bean.PermissionBean;


@Repository
public interface PermissionRepository extends BaseRepository<PermissionBean, Long> {

}
