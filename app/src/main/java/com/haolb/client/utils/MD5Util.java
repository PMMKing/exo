package com.haolb.client.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类�?
 * 
 * @author Michael J Chane
 * @version $Revision: 1.2 $ $Date: 2009/08/30 08:52:57 $
 */
public final class MD5Util {

    /**
     * 十六进制MD5编码长度
     */
    private static final int HEX_DIGEST_LENGTH = 32;

    /**
     * 十六进制字符
     */
    private static final char[] HEX_CHAR = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f' };

    /**
     * 不需要实例化
     */
    private MD5Util() {
    }

    /**
     * 对指定的文本信息进行MD5摘要，并以十六进制格式返回�?
     * 
     * @param text 要进行MD5摘要的文本信�?
     * @return 十六进制的MD5摘要
     * @throws Exception 如果处理中发生异常的�?
     */
    public static String hexDigest(String text) throws NoSuchAlgorithmException {
        return hexDigest(text.getBytes());
    }

    /**
     * 对指定的信息进行MD5摘要，并以十六进制格式返回�?
     * 
     * @param bytes 要进行MD5摘要的字节信�?
     * @return 十六进制的MD5摘要
     * @throws java.security.NoSuchAlgorithmException
     * @throws Exception 如果处理中发生异常的�?
     */
    public static String hexDigest(byte[] bytes) throws NoSuchAlgorithmException {
        // 信息摘要�?
        MessageDigest md = MessageDigest.getInstance("MD5");
        // 获得MD5信息摘要
        byte[] md5Bytes = md.digest(bytes);
        // 返回十六进制的MD5摘要
        return toHexString(md5Bytes);
    }

    /**
     * 将MD5的字节码表现为十六进制编码�?
     * 
     * @param md5Bytes MD5的字节码
     * @return 十六进制编码
     */
    private static String toHexString(byte[] md5Bytes) {
        StringBuilder buff = new StringBuilder(HEX_DIGEST_LENGTH);
        for (byte b : md5Bytes) {
            int unsignedByte = b & 0xFF;
            int high = unsignedByte >> 4;
            int low = unsignedByte & 0xF;
            buff.append(HEX_CHAR[high]).append(HEX_CHAR[low]);
        }

        return buff.toString();
    }

}