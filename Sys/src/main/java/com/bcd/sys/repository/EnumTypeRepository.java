package com.bcd.sys.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.bean.EnumTypeBean;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/5/18.
 */
@Repository
public interface EnumTypeRepository extends BaseRepository<EnumTypeBean,Long> {
}
