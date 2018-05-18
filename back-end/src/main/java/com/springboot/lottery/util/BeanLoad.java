package com.springboot.lottery.util;

import java.util.Random;
import java.util.UUID;
/**
 * 随机数工具类
 * @author Administrator
 *
 */
public class BeanLoad {
	
	public static String LETTER_NUMBER = "abcdefghijklmnopqrstuvwxyz1234567890";
	public static String MAJUSCULE_NUMBER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	/**
	 * 主键id
	 * @return String
	 */
	public static String getId() {
		// 创建随机数对象
		Random random = new Random();
		// 获取当前时间戳
		long current = System.currentTimeMillis();
		String str = "";
		// 随机生成字符串
		for (int i = 0; i < 2; i++) {
			int number = random.nextInt(LETTER_NUMBER.length());
			str += LETTER_NUMBER.charAt(number);
		}
		// 把随机生成的字符串与时间戳拼接
		str += current;
		return str;
	}
	/**
	 * UUID随机数
	 * @return String
	 */
	public static String getUUID() {
		//随机生成32位字母与数字
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid;
	}
	
	/**
	 * 时间戳
	 * @return
	 */
	public static String getimeMillis() {
		long timeMillis = System.currentTimeMillis();
		return String.valueOf(timeMillis);
	}
	
	/**
	 * 订单号
	 * @return
	 */
	public static String getNumber() {
		// 创建随机数对象
		Random random = new Random();
		// 获取当前时间戳
		long current = System.currentTimeMillis();
		String str = "";
		// 随机生成字符串
		for (int i = 0; i < 2; i++) {
			int number = random.nextInt(MAJUSCULE_NUMBER.length());
			str += MAJUSCULE_NUMBER.charAt(number);
		}
		// 把随机生成的字符串与时间戳拼接
		str += current;
		return str;
	}
}
