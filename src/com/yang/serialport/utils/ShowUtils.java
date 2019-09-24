package com.yang.serialport.utils;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * 提示框
 * 
 * @author DengGuiHui
 */
public class ShowUtils {
	
	private static JTextArea showDataView  = null ;
	private static StringBuffer cache= new StringBuffer();
	
	public static void setShowView(JTextArea mDataView) {
		showDataView  = mDataView;
	}

	public static void appendln(final String data) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		cache.append(data+ "\r\n");
        		showDataView.setText(cache.toString());	
            }
        });	
	}	
	
	public static void clearReceiveData() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		cache.delete(0, cache.length()-1);
        		showDataView.setText(cache.toString());	
            }
        });
	}
	
	/**
	 * 消息提示
	 * 
	 * @param message
	 *            消息内容
	 */
	public static void message(String message) {
		JOptionPane.showMessageDialog(null, message);
	}

	/**
	 * 警告消息提示
	 * 
	 * @param message
	 *            消息内容
	 */
	public static void warningMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		JOptionPane.showMessageDialog(null, message, "警告",
        				JOptionPane.WARNING_MESSAGE);
            	}
            });
	}

	/**
	 * 错误消息提示
	 * 
	 * @param message
	 *            消息内容
	 */
	public static void errorMessage(String message) {
		JOptionPane.showMessageDialog(null, message, "错误",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 自定义的消息提示
	 * 
	 * @param title
	 *            标题
	 * @param message
	 *            消息内容
	 */
	public static void plainMessage(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * 带有选择功能的提示
	 * 
	 * @param title
	 *            标题
	 * @param message
	 *            消息内容
	 * @return 是/否 0/1
	 */
	public static boolean selectMessage(String title, String message) {
		int isConfirm = JOptionPane.showConfirmDialog(null, message, title,
				JOptionPane.YES_NO_OPTION);
		if (0 == isConfirm) {
			return true;
		}
		return false;
	}
}
