package com.bcd.dbinfo.mysql.repository;

import com.bcd.dbinfo.mysql.bean.ColumnsBean;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnsRepository extends JpaSpecificationExecutor<ColumnsBean>{
}
