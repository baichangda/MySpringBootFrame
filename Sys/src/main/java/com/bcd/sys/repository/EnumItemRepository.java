package com.bcd.sys.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.bean.EnumItemBean;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/5/18.
 */
@Repository
public interface EnumItemRepository extends BaseRepository<EnumItemBean,Long> {
}
