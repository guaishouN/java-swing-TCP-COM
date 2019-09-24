package com.yang.serialport.tcp;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.yang.serialport.manager.SerialPortManager;
import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.ShowUtils;

import java.nio.channels.ServerSocketChannel;

public class ServerHandlerImpl implements ServerHandlerBs {
    private int bufferSize = 1024;
    private String localCharset = "UTF-8";
    private ArrayList<SocketChannel> socketChannelList = new ArrayList<>();
    
    public ServerHandlerImpl() {
    }

    public ServerHandlerImpl(int bufferSize) {
        this(bufferSize, null);
    }

    public ServerHandlerImpl(String localCharset) {
        this(-1, localCharset);
    }

    public ServerHandlerImpl(int bufferSize, String localCharset) {
        this.bufferSize = bufferSize > 0 ? bufferSize : this.bufferSize;
        this.localCharset = localCharset == null ? this.localCharset : localCharset;
    }

    @Override
    public void closeAllConnect() {
    	for(SocketChannel sc:socketChannelList) {
    		try {
    			sc.socket().close();
				sc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	socketChannelList.clear();
    }
    
    @Override
    public void broadCastData(byte[] data) {
    	if(data==null) {
    		return;
    	}
    	for(SocketChannel sc:socketChannelList) {
    		try {
    			ByteBuffer buffer = ByteBuffer.allocate(data.length);
    			buffer.put(data);
    	        buffer.flip();
    	        sc.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    @Override
    public void handleAccept(SelectionKey selectionKey) throws IOException {
        //获取channel
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        //非阻塞
        socketChannel.configureBlocking(false);
        //注册selector
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
        String ip = socketChannel.getRemoteAddress().toString();
        ShowUtils.appendln("TCP客户端["+ip+"]连接到tcp服务");
        socketChannelList.add(socketChannel);
    }

    @Override
    public String handleRead(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int readLen =socketChannel.read(buffer);
        String receivedStr = "";
        String ip = socketChannel.getRemoteAddress().toString();
        if (readLen == -1) {
            //没读到内容关闭
            socketChannel.shutdownOutput();
            socketChannel.shutdownInput();
            socketChannel.close();
            ShowUtils.appendln("TCP客户端["+ip+"]连接断开......");
        } else {
            byte[] result = new byte[readLen];
            buffer.flip();
            buffer.get(result);
            //将channel改为读取状态
            if(NetUtils.isHexShow()) {
            	String curComName = SerialPortManager.getCurrentCommName();            	
                if(!SerialPortManager.isClosed()) {
                	SerialPortManager.sendToPort(result);
                	ShowUtils.appendln(curComName+"<<<TCP<<<< "+ByteUtils.byteArrayToHexString(result));
                }else {
                	ShowUtils.appendln("TCP<<<< "+ByteUtils.byteArrayToHexString(result));
                }
            }else {
	            //按照编码读取数据
	            receivedStr = Charset.forName(localCharset).newDecoder().decode(buffer).toString();
            	String curComName = SerialPortManager.getCurrentCommName();            	
                if(!SerialPortManager.isClosed()) {
                	SerialPortManager.sendToPort(result);
                	ShowUtils.appendln(curComName+"<<<TCP<<<< "+receivedStr);
                }else {
                	ShowUtils.appendln("TCP<<<< "+receivedStr);
                }
            }
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
        }
        return receivedStr;
    }
}
