package com.bcd;

import io.xjar.XCryptos;

public class TestUtil {

    public static void main(String[] args) throws Exception {
        XCryptos
                .encryption()
                .from("/Users/baichangda/bcd/workspace/MySpringBootFrame/build/libs/MySpringBootFrame-1.0-SNAPSHOT.jar")
                .use("test")
                .include("/com/bcd/**/*.class")
                .to("/Users/baichangda/xjartest/result.jar");
    }
}
