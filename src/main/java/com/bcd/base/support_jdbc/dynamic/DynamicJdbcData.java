package com.bcd.base.support_jdbc.dynamic;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public record DynamicJdbcData(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
}