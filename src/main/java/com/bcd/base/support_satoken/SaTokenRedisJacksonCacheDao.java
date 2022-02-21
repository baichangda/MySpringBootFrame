package com.bcd.base.support_satoken;

import cn.dev33.satoken.dao.SaTokenDao;

import java.util.List;

public class SaTokenRedisJacksonCacheDao implements SaTokenDao {
    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void set(String key, String value, long timeout) {

    }

    @Override
    public void update(String key, String value) {

    }

    @Override
    public void delete(String key) {

    }

    @Override
    public long getTimeout(String key) {
        return 0;
    }

    @Override
    public void updateTimeout(String key, long timeout) {

    }

    @Override
    public Object getObject(String key) {
        return null;
    }

    @Override
    public void setObject(String key, Object object, long timeout) {

    }

    @Override
    public void updateObject(String key, Object object) {

    }

    @Override
    public void deleteObject(String key) {

    }

    @Override
    public long getObjectTimeout(String key) {
        return 0;
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {

    }

    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size) {
        return null;
    }
}
