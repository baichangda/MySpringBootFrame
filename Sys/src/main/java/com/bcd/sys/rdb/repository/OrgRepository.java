package com.bcd.sys.rdb.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.rdb.bean.OrgBean;
import org.springframework.stereotype.Repository;

/**
 * 组织机构基础信息操作
 *
 * @author Aaric
 * @since 2017-04-28
 */
@Repository
public interface OrgRepository extends BaseRepository<OrgBean, Long> {
}
