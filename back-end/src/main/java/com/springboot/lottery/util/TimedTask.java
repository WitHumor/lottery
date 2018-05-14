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
	
//	@Scheduled(fixedDelay = 1000)
//	public void singleNote() {
//		System.out.println(Thread.currentThread().getName() + "定时任务启动");
//		String member = null;
//		try {
//			member = memberController.singleNoteAccount();
//			if(member == null) {
//				System.out.println("-----");
//			}
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//	}
}
