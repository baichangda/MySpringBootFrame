package com.bcd.rdb.jdbc.dynamic;

import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@Getter
public class DynamicJdbcData {
    public DynamicJdbcData(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
}