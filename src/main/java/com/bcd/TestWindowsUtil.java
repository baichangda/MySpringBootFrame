package com.bcd;

import io.xjar.XCryptos;

public class TestWindowsUtil {
    public static void main(String[] args) throws Exception {
        XCryptos
                .encryption()
                .from("D:\\workspace\\MySpringBootFrame\\build\\libs\\MySpringBootFrame-1.0-SNAPSHOT.jar")
                .use("test")
                .include("/com/bcd/**/*.class")
                .to("D:\\xjartest\\result.jar");
    }
}
