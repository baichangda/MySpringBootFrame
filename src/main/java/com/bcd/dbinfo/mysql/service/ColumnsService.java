package com.bcd.dbinfo.mysql.service;

import com.bcd.base.condition.Condition;
import com.bcd.dbinfo.mysql.bean.ColumnsBean;
import com.bcd.dbinfo.mysql.repository.ColumnsRepository;
import com.bcd.rdb.util.ConditionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColumnsService{
    @Autowired
    private ColumnsRepository columnsRepository;

    public List<ColumnsBean> list(Condition condition){
        return columnsRepository.findAll(ConditionUtil.toSpecification(condition));
    }
}
