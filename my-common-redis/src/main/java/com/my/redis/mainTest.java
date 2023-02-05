package com.my.redis;


import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class mainTest {
    public static void main(String[] args) throws UnsupportedEncodingException {
        byte[] encode = Base64.encodeBase64("123456".getBytes(StandardCharsets.UTF_8));
        String string = new String(encode,"utf-8");

        System.out.println(string);
    }
}
