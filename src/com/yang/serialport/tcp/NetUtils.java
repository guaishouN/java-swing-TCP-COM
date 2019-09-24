package com.yang.serialport.tcp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.yang.serialport.utils.ShowUtils;

public class NetUtils {
	
	public static ArrayBlockingQueue<byte[]> sendBytes = new ArrayBlockingQueue<>(100);
	public static ArrayBlockingQueue<byte[]> getSendBytes(){
		return sendBytes;
	}
	
	public static void postTcpSendData(byte[] data) {
		sendBytes.add(data);
	}
	
	private static boolean isHexShow = true;
	
	public static void setIsHexShow(boolean chooseHex) {
		isHexShow = chooseHex;
	}
	
	public static boolean isHexShow() {
		return isHexShow;
	}
	
	public static int port  = 7749;	
	
	public static void setPort(int usedPort) {
		port = usedPort;
	}
	
	public static int getPort() {
		return port;
	}
	/**
	 * 获取本地单一IP地址
	 * @return
	 */
	public static String getLocalSingleIpAddr() {
		String localip= "";
        InetAddress ia=null;
        try {
            ia=ia.getLocalHost();            
            String localname=ia.getHostName();
            localip=ia.getHostAddress();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return localip;
	}
	
	
	
	/**
	 * 获取本机IP地址列表
	 * @return
	 */
	public static List<String> getLocalIpAddr() {
        List<String> ipList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            String ip;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) { // IPV4
                        ip = inetAddress.getHostAddress();
                        ipList.add(ip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }  
		return ipList;
	}
}
