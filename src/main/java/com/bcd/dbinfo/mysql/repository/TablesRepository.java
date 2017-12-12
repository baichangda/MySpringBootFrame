package com.bcd.dbinfo.mysql.repository;

import com.bcd.dbinfo.mysql.bean.TablesBean;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TablesRepository extends JpaSpecificationExecutor<TablesBean>{
}
