package com.bcd.sys.rdb.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.rdb.bean.TaskBean;
import org.springframework.stereotype.Repository;


@Repository
public interface TaskRepository extends BaseRepository<TaskBean, Long> {

}
