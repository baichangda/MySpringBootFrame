package com.bcd.sys.repository;

import com.bcd.base.support_jpa.repository.BaseRepository;
import com.bcd.sys.bean.RoleBean;
import org.springframework.stereotype.Repository;


@Repository
public interface RoleRepository extends BaseRepository<RoleBean, Long> {

}
