package com.springboot.lottery.config;


import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * 数据库配置
 * @author Administrator
 *
 */
@Configuration // 用于取代 XML 来配置 Spring 
@AutoConfigureAfter(LotterySpringBootStart.class)
public class LotteryMapperConfig {
	/**
	 * 配置访问数据库的包名
	 * @return
	 */
	@Bean
	public MapperScannerConfigurer mapperScannerConfigurer() {
		MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
		mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
		mapperScannerConfigurer.setBasePackage("com.springboot.lottery.mybatis");
		return mapperScannerConfigurer;
	}
	
}
