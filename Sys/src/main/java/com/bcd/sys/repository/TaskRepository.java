package com.bcd.sys.repository;

import com.bcd.rdb.repository.BaseRepository;
import org.springframework.stereotype.Repository;
import com.bcd.sys.bean.TaskBean;


@Repository
public interface TaskRepository extends BaseRepository<TaskBean, Long> {

}
