package com.bcd.base.support_rdb.dbinfo.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DBInfo {
    private String url;
    private String username;
    private String password;
    private String db;
}