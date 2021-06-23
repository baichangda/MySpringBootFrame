package com.bcd.base.support_baidu;

import java.io.IOException;

public class BaiduHttpTest {
    public static void main(String[] args) throws IOException {
        BaiduHttp.instance.orc("1","2","3").execute();
    }
}
