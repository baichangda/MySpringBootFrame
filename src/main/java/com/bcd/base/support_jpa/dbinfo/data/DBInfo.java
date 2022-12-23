package com.bcd.base.support_jpa.dbinfo.data;

public class DBInfo {
    public final String url;
    public final String username;
    public final String password;
    public final String db;

    public DBInfo(String url, String username, String password, String db) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.db = db;
    }
}