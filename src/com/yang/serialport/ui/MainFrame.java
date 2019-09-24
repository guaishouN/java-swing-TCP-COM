package com.yang.serialport.ui;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.yang.serialport.manager.SerialPortManager;
import com.yang.serialport.tcp.NetUtils;
import com.yang.serialport.tcp.NioSocketServer;
import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.ShowUtils;

import gnu.io.PortInUseException;
import gnu.io.SerialPort;

/**
 * 主界面
 * 
 * @author DengGuihui
 */
@SuppressWarnings("all")
public class MainFrame extends JFrame {

	// 程序界面宽度
	public final int WIDTH = 530;
	// 程序界面高度
	public final int HEIGHT = 495;

	public static String commName = "null";
	
	// 数据显示区
	private JTextArea mDataView = new JTextArea();
	private JScrollPane mScrollDataView = new JScrollPane(mDataView);

	// 串口设置面板
	private JPanel mSerialPortPanel = new JPanel();
	private JLabel mSerialPortLabel = new JLabel("串口");
	private JLabel mBaudrateLabel = new JLabel("波特率");
	private JComboBox mCommChoice = new JComboBox();
	private JComboBox mBaudrateChoice = new JComboBox();
	private ButtonGroup mDataChoice = new ButtonGroup();
	private JRadioButton mDataASCIIChoice = new JRadioButton("ASCII");
	private JRadioButton mDataHexChoice = new JRadioButton("Hex", true);

	// 操作面板
	private JPanel mOperatePanel = new JPanel();
	private JTextArea mDataInput = new JTextArea();
	private JScrollPane mScrollDataInputView = new JScrollPane(mDataInput);
	private JButton mSerialPortOperate = new JButton("打开串口");
	private JButton mSendData = new JButton("发送数据");

	
	//串口转发TCP服务
	private JPanel mTcpServicePanel = new JPanel();
	private JLabel mTcpIPPortLabel = new JLabel("端口");
	private JTextArea tcpPort = new JTextArea();
	private JButton mStartTCPListening = new JButton("开启TCP转发服务");
	
	// 串口列表
	private List<String> mCommList = null;
	// 串口对象
	private SerialPort mSerialport;

	//TCP Service
	NioSocketServer nioService;
	public MainFrame() {
		ShowUtils.setShowView(mDataView);
		initView();
		initComponents();
		actionListener();
		initData();
	}

	/**
	 * 初始化窗口
	 */
	private void initView() {
		// 关闭程序
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		// 禁止窗口最大化
		setResizable(false);

		// 设置程序窗口居中显示
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
		this.setLayout(null);

		setTitle("联强邦盛   TCP与串口通道转换  v1.0");
	}

	/**
	 * 初始化控件
	 */
	private void initComponents() {
		// 数据显示
		mDataView.setFocusable(true);
		mScrollDataView.setBounds(10, 10, 505, 200);
		add(mScrollDataView);

		// 串口设置
		mSerialPortPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
		mSerialPortPanel.setBounds(10, 220, 170, 130);
		mSerialPortPanel.setLayout(null);
		add(mSerialPortPanel);

		mSerialPortLabel.setForeground(Color.gray);
		mSerialPortLabel.setBounds(10, 25, 40, 20);
		mSerialPortPanel.add(mSerialPortLabel);

		mCommChoice.setFocusable(false);
		mCommChoice.setBounds(60, 25, 100, 20);
		mSerialPortPanel.add(mCommChoice);

		mBaudrateLabel.setForeground(Color.gray);
		mBaudrateLabel.setBounds(10, 60, 40, 20);
		mSerialPortPanel.add(mBaudrateLabel);

		mBaudrateChoice.setFocusable(false);
		mBaudrateChoice.setBounds(60, 60, 100, 20);
		mSerialPortPanel.add(mBaudrateChoice);

		mDataASCIIChoice.setBounds(20, 95, 55, 20);
		mDataHexChoice.setBounds(95, 95, 55, 20);
		mDataChoice.add(mDataASCIIChoice);
		mDataChoice.add(mDataHexChoice);
		mSerialPortPanel.add(mDataASCIIChoice);
		mSerialPortPanel.add(mDataHexChoice);

		// 操作
		mOperatePanel.setBorder(BorderFactory.createTitledBorder("串口发送数据"));
		mOperatePanel.setBounds(200, 220, 315, 230);
		mOperatePanel.setLayout(null);
		add(mOperatePanel);
			
		mScrollDataInputView.setBounds(25, 25, 265, 150);
		mDataInput.setLineWrap(true);
		mDataInput.setWrapStyleWord(true);
		mDataInput.setAutoscrolls(true);
		mOperatePanel.add(mScrollDataInputView);

		mSerialPortOperate.setFocusable(false);
		mSerialPortOperate.setBounds(45, 195, 90, 20);
		mOperatePanel.add(mSerialPortOperate);

		mSendData.setFocusable(false);
		mSendData.setBounds(180, 195, 90, 20);
		mOperatePanel.add(mSendData);
		
		//TCP转发服务
		String localIP = NetUtils.getLocalSingleIpAddr();
		mTcpServicePanel.setBorder(BorderFactory.createTitledBorder("本机IP "+localIP));
		mTcpServicePanel.setBounds(10, 220+140, 170, 90);
		mTcpServicePanel.setLayout(null);
		add(mTcpServicePanel);
		mTcpIPPortLabel.setBounds(10, 25, 170, 20);
		mTcpServicePanel.add(mTcpIPPortLabel);
		tcpPort.setBounds(10+30, 25, 40, 20);
		tcpPort.setLineWrap(true);
		tcpPort.setWrapStyleWord(true);
		tcpPort.setBackground(Color.cyan);
		tcpPort.setText("7749");
		mTcpServicePanel.add(tcpPort);
		mStartTCPListening.setFocusable(false);
		mStartTCPListening.setBounds(10, 25+30, 150, 20);
		mTcpServicePanel.add(mStartTCPListening);

	}

	/**
	 * 初始化数据
	 */
	private void initData() {		
		mCommList = SerialPortManager.findPorts();
		// 检查是否有可用串口，有则加入选项中
		if (mCommList == null || mCommList.size() < 1) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			for (String s : mCommList) {
				mCommChoice.addItem(s);
			}
		}

		mBaudrateChoice.addItem("9600");
		mBaudrateChoice.addItem("19200");
		mBaudrateChoice.addItem("38400");
		mBaudrateChoice.addItem("57600");
		mBaudrateChoice.addItem("115200");
	}

	/**
	 * 按钮监听事件
	 */
	private void actionListener() {
		// 串口
		mCommChoice.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				mCommList = SerialPortManager.findPorts();
				// 检查是否有可用串口，有则加入选项中
				if (mCommList == null || mCommList.size() < 1) {
					ShowUtils.warningMessage("没有搜索到有效串口！");
				} else {
					int index = mCommChoice.getSelectedIndex();
					mCommChoice.removeAllItems();
					for (String s : mCommList) {
						mCommChoice.addItem(s);
					}
					mCommChoice.setSelectedIndex(index);
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// NO OP
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// NO OP
			}
		});

		// 打开|关闭串口
		mSerialPortOperate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ("打开串口".equals(mSerialPortOperate.getText()) && mSerialport == null) {
					openSerialPort(e);
				} else {
					closeSerialPort(e);
				}
			}
		});

		// 发送数据
		mSendData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendData(e);
			}
		});
		
		//数据显示格式选择
		ActionListener hexListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (mDataASCIIChoice.isSelected()) {
					NetUtils.setIsHexShow(false);
				}
				if (mDataHexChoice.isSelected()) {
					NetUtils.setIsHexShow(true);
				}
			}			
		};
		
		mDataHexChoice.addActionListener(hexListener);
		mDataASCIIChoice.addActionListener(hexListener);
		
		//打开TCP数据服务
		mStartTCPListening.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {					
					if(nioService!=null&&nioService.isTcpStart()) {
						 nioService.closeTcpServer();
						 mStartTCPListening.setText("开启TCP转发服务");
						return;
					}
					String ip = NetUtils.getLocalSingleIpAddr();
					int port = Integer.valueOf(tcpPort.getText().toString());
					NetUtils.setPort(port);
					nioService = new NioSocketServer(ip,port);
					nioService.start();
					mStartTCPListening.setText("关闭TCP转发服务");
				}catch(Exception e) {
					ShowUtils.warningMessage("TCP服务处理失败！！");
				}
			}			
		});
				
	}

	/**
	 * 打开串口
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void openSerialPort(java.awt.event.ActionEvent evt) {
		// 获取串口名称
		commName = (String) mCommChoice.getSelectedItem();
		// 获取波特率，默认为9600
		int baudrate = 9600;
		String bps = (String) mBaudrateChoice.getSelectedItem();
		baudrate = Integer.parseInt(bps);

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			try {
				mSerialport = SerialPortManager.openPort(commName, baudrate);
				if (mSerialport != null) {
					ShowUtils.appendln(commName+"串口已打开");
					mSerialPortOperate.setText("关闭串口");
				}
			} catch (PortInUseException e) {
				ShowUtils.warningMessage(commName+"串口已被占用！");
			}
		}

		// 添加串口监听
		SerialPortManager.addListener(mSerialport, new SerialPortManager.DataAvailableListener() {

			@Override
			public void dataAvailable() {
				byte[] data = null;
				try {
					if (mSerialport == null) {
						ShowUtils.errorMessage("串口对象为空，监听失败！");
					} else {
						// 读取串口数据
						data = SerialPortManager.readFromPort(mSerialport);

						// 以字符串的形式接收数据
						if (mDataASCIIChoice.isSelected()) {
							if(nioService==null || !nioService.isTcpStart()) {
								ShowUtils.appendln(commName+">>>> "+new String(data));
							}else {
								NetUtils.postTcpSendData(data);
							}							
						}

						// 以十六进制的形式接收数据
						if (mDataHexChoice.isSelected()) {
							if(nioService==null || !nioService.isTcpStart()) {
								ShowUtils.appendln(commName+">>>> "+ByteUtils.byteArrayToHexString(data));
							}else {
								NetUtils.postTcpSendData(data);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 关闭串口
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void closeSerialPort(java.awt.event.ActionEvent evt) {
		SerialPortManager.closePort(mSerialport);
		ShowUtils.appendln(commName+"串口已关闭");
		mSerialPortOperate.setText("打开串口");
		mSerialport = null;
	}

	/**
	 * 发送数据
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void sendData(java.awt.event.ActionEvent evt) {
		// 待发送数据
		String data = mDataInput.getText().toString();

		if (mSerialport == null) {
			ShowUtils.warningMessage("请先打开串口！");
			return;
		}

		if ("".equals(data) || data == null) {
			ShowUtils.warningMessage("请输入要发送的数据！");
			return;
		}

		// 以字符串的形式发送数据
		if (mDataASCIIChoice.isSelected()) {
			SerialPortManager.sendToPort(mSerialport, data.getBytes());			
		}

		// 以十六进制的形式发送数据
		if (mDataHexChoice.isSelected()) {		
			SerialPortManager.sendToPort(mSerialport, ByteUtils.hexStr2Byte(data));
		}
		
		ShowUtils.appendln(commName+"<<<< "+data);
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				System.out.println("打开");
				MainFrame mainFrame = new MainFrame();
				mainFrame.setVisible(true);				
			}
		});				
	}
}