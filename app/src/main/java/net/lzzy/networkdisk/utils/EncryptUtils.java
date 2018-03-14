package net.lzzy.networkdisk.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 *   加密解密相关的工具类
 */
public class EncryptUtils {

    /**
     * MD5加密
     *
     * @param data 明文字符串
     * @return 16进制密文
     */
    public static String encryptMD5ToString(String data) {
        return encryptMD5ToString(data.getBytes());
    }

    /**
     * MD5加密
     *
     * @param data 明文字节数组
     * @return 16进制密文
     */
     private static String encryptMD5ToString(byte[] data) {
        return ConvertUtils.bytes2HexString(encryptMD5(data));
    }

    /**
     * MD5加密
     *
     * @param data 明文字符串
     * @param salt 盐
     * @return 16进制加盐密文
     */
    public static String encryptMD5ToString(String data, String salt) {
        return ConvertUtils.bytes2HexString(encryptMD5((data + salt).getBytes()));
    }


    /**
     * MD5加密
     *
     * @param data 明文字节数组
     * @return 密文字节数组
     */
     private static byte[] encryptMD5(byte[] data) {
        return hashTemplate(data);
    }

    /**
     * hash加密模板
     *
     * @param data      数据
     * @return 密文字节数组
     */
    private static byte[] hashTemplate(byte[] data) {
        if (data == null || data.length <= 0) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
