package com.bcd.sys.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.bean.LogBean;
import org.springframework.stereotype.Repository;

/**
 * Created by incar on 2017/5/4.
 */
@Repository
public interface LogRepository extends BaseRepository<LogBean,Long> {

}
