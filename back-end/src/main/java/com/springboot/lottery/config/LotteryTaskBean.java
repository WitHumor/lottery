package com.springboot.lottery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@Configuration // 用于取代 XML 来配置 Spring 
public class LotteryTaskBean {
	/**
	 * 定时任务线程池配置
	 * @return
	 */
	@Bean
	public TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		//线程池大小
		taskScheduler.setPoolSize(10);
		return taskScheduler;
	}
}
