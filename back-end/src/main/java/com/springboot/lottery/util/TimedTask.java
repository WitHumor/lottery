package com.springboot.lottery.util;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.springboot.lottery.controller.MemberController;

/**
 * 任务类
 * @author Administrator
 *
 */
@Component // component是spring扫描组件时的标识
public class TimedTask {
	
	@Autowired
	private MemberController memberController;
	
	/**
	 * 3秒查找一次赛果
	 */
	@Scheduled(fixedDelay = 3000)
	public void singleNote() {
		System.out.println(Thread.currentThread().getName() +"足球篮球结算定时任务启动..");
		boolean member;
		try {
			member = memberController.singleNoteAccount();
			if(member) {
				System.out.println("---------");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 每隔一分钟查询超过24小时未结算的结果
	 */
	@Scheduled(cron = "0 0/1 * * * ?")
	public void amidithionOvertime() {
		System.out.println(Thread.currentThread().getName() + "扫描超过24小时未结算足球篮球定时任务启动..");
		boolean amidithion = memberController.amidithionOvertime();
		if(amidithion == false) {
			System.err.println("无超过24小时未结算数据");
		}
	}
	
	/**
	 * 每月一号凌晨1分结算返利金额
	 */
	@Scheduled(cron = "0 1 0 1 * ?")
	public void accountRebate() {
		System.out.println(Thread.currentThread().getName() + "结算返利金额定时任务启动..");
		memberController.accountRebate();
	}
}
