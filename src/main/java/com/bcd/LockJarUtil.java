package com.bcd;

import io.xjar.XCryptos;

public class TestUtil {

    public static void main(String[] args) throws Exception {
        XCryptos
                .encryption()
                .from("/Users/baichangda/hlj/workspace/bwt-vms-electrice-fence/target/electricfence-1.0-SNAPSHOT.jar")
                .use("test")
                .include("/com/bwt/**/*.class")
                .to("/Users/baichangda/xjartest/result.jar");
    }
}
