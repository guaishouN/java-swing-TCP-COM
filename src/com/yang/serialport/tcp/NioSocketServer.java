package com.yang.serialport.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.yang.serialport.manager.SerialPortManager;
import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.ShowUtils;

public class NioSocketServer {
	//两条线程,一条接受链接及收数据，一条发送数据
	public ExecutorService excutor = Executors.newFixedThreadPool(2);
	public String localIP = "";
	public int localPort ;
    private volatile byte flag = 0;
	private ServerSocketChannel serverSocketChannel =null;
	private ServerHandlerBs handler;
	private Selector selector=null;
    public NioSocketServer(String ip, int port) {
    	this.localIP = ip;
    	this.localPort = port;
    }
    
    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public void runSendDataThread() {
    	final ArrayBlockingQueue<byte[]> sendBytes = NetUtils.getSendBytes();
    	excutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					while(flag==1) {
						byte[] data = sendBytes.take();
						if(handler!=null && data!=null) {
							handler.broadCastData(data);
							String commName = SerialPortManager.getCurrentCommName();
							if(!NetUtils.isHexShow()) {
								ShowUtils.appendln(commName+">>>>TCP>>>> "+new String(data));
							}else {
								ShowUtils.appendln(commName+">>>>TCP>>>> "+ByteUtils.byteArrayToHexString(data));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}    		
    	});
    }   
    
    public void start() {
		flag = 1;
    	//开启TCP发送数据线程
        runSendDataThread();
        //开启TCP接受连接及读数据线程
    	excutor.execute(new Runnable() {
			@Override
			public void run() {
			    //创建serverSocketChannel，监听localPort端口
			    try {
			    	serverSocketChannel = ServerSocketChannel.open();
			        serverSocketChannel.socket().bind(new InetSocketAddress(localPort));
			        //设置为非阻塞模式
			        serverSocketChannel.configureBlocking(false);
			        //为serverChannel注册selector
			        selector = Selector.open();
				    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			        ShowUtils.appendln("已打开TCP服务打开监听.....\r\n连接"+localIP+":"+localPort+"可以串口数据TCP转发");
			        //创建消息处理器
			        handler = new ServerHandlerImpl(1024);
			        while (flag == 1) {
			            selector.select();
			            if(flag == 0) {
			            	break;
			            }            
			            //获取selectionKeys并处理
			            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
			            while (keyIterator.hasNext()) {
			                SelectionKey key = keyIterator.next();
			                try {
			                    //连接请求
			                    if (key.isAcceptable()) {
			                        handler.handleAccept(key);	
			                    }
			                    //读请求
			                    if (key.isReadable()) {
			                        System.out.println(handler.handleRead(key));
			                    }
			                } catch (IOException e) {
			                    e.printStackTrace();
			                }
			                //处理完后移除当前使用的key
			                keyIterator.remove();
			            }
			        }
			    } catch (IOException e) {
			        //e.printStackTrace();
			        ShowUtils.warningMessage("端口["+localPort+"]已被占用请重新选择");
			    }
			}    		
    	});

    }
    
    /**
             * 关闭TCP服务
     */
    public void closeTcpServer() {    	
    	if(serverSocketChannel!=null) {
    		try {    			
    			flag =0;
    			selector.close();
    			serverSocketChannel.socket().close();
				serverSocketChannel.close();
				excutor.shutdownNow();
				if(handler!=null) {
					handler.closeAllConnect();
				}
				ShowUtils.appendln("端口["+localPort+"]TCP服务已关闭 ");
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}

    }
    
    public boolean isTcpStart() {
    	return flag == 1;
    }
}
