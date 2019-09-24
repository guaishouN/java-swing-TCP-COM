package com.yang.serialport.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Byte转换工具
 * 
 * @author DengGuiHui
 */
public class ByteUtils {
	 private static final char[] hexCode = "0123456789ABCDEF".toCharArray();
	 private static final String EMPTY_STR = "";
	 private final static byte[] EMPTY_BYTES= new byte[] {};

	/**
	 * 十六进制字符串转byte[]
	 * 
	 * @param hex 十六进制字符串
	 * @return byte[]
	 */
	public static byte[] hexStr2Byte(String data) {
		if (data == null) {
			return EMPTY_BYTES;
		}
		String hex=data.replaceAll(" ", "");
		hex.toUpperCase();
		int length = hex.length();
		if(length%2>0) {
			hex+='0';
			length ++;
		}
		byte b1 = -1;
		byte b2 = -1;		
		ByteBuffer buffer = ByteBuffer.allocate(length/2);
		for (int i = 0; i < length; i++) {
			for(int k=0;k<hexCode.length;k++) {
				if(hexCode[k]==hex.charAt(i)) {
					b1 = (byte)k;
				}				
			}
			i++;
			for(int k=0;k<hexCode.length;k++) {
				if(hexCode[k]==hex.charAt(i)) {
					b2 = (byte)k;
				}				
			}
			if(b1 == -1 || b2 == -1) {
				return EMPTY_BYTES;
			}
			byte ab = (byte)((byte)(b1<<4)| b2);
			buffer.put(ab);
			b1 = -1;
			b2 = -1;	
		}
		return buffer.array();
	}
	
    /**
     * byte[]转换为 hex字符串
     * @param data byte[]
     * @return String
     */
    public static String byteArrayToHexString(byte[] data) {
        if (data==null || data.length==0){
            return EMPTY_STR;
        }
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
            r.append(' ');
        }
        return r.toString();
    }

	/**
	 * byte转十六进制字符
	 * 
	 * @param b
	 *            byte
	 * @return 十六进制字符
	 */
	public static String byteToHex(byte b) {
		String hex = Integer.toHexString(b & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		return hex.toUpperCase(Locale.getDefault());
	}
}
