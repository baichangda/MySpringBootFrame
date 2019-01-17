package com.bcd.sys.repository;

import com.bcd.rdb.repository.BaseRepository;
import com.bcd.sys.bean.TaskBean;
import org.springframework.stereotype.Repository;


@Repository
public interface TaskRepository extends BaseRepository<TaskBean, Long> {

}
