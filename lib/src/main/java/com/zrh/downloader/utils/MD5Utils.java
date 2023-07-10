package com.zrh.downloader.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author zrh
 * @date 2023/7/6
 */
public class MD5Utils {
    public static String md5(String text) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            messageDigest.update(text.getBytes(StandardCharsets.UTF_8));
            byte[] code = messageDigest.digest();
            return hex(code);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String hex(byte[] bytes) {
        if (bytes.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int value = b & 0xFF;
                String hex = Integer.toHexString(value).toLowerCase(Locale.ROOT);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        }
        return "";
    }
}
