package com.sys.repository;

import com.base.repository.BaseRepository;
import com.sys.bean.RoleBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色操作
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Repository
public interface RoleRepository extends BaseRepository<RoleBean, Long> {

}
