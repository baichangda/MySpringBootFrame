package com.bcd.base.support_jdbc.dynamic;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class DynamicJdbcData {
    public final JdbcTemplate jdbcTemplate;
    public final TransactionTemplate transactionTemplate;

    public DynamicJdbcData(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }
}