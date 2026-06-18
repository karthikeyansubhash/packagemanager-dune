package com.hp.jetadvantage.link.pkgmgt.hpkutil.utils;

import javax.xml.bind.DatatypeConverter;

public class SecurityHelper {

    public static String encodeAuth(String authorization) {
        String data = "admin:" + authorization;
        String encoded = "";
        try {
            encoded = DatatypeConverter.printBase64Binary(data.getBytes("UTF-8"));
        } catch (Exception e) {}
        return encoded;
    }
}
