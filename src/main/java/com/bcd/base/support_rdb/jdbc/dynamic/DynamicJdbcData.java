package com.bcd.base.rdb.jdbc.dynamic;

import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Getter
public class DynamicJdbcData {
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    public DynamicJdbcData(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }
}