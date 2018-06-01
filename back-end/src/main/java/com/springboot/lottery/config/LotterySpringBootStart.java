package com.springboot.lottery.config;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * 启动服务器类
 * 
 * @author Administrator
 *
 */
@EnableCaching // 扫描@cache..缓存注解
@EnableScheduling // 扫描定时任务@Scheduled注解
@SpringBootApplication // 指明当前类为springboot启动类
@PropertySource("classpath:jdbc.properties") // 扫描数据库配置文件
@ComponentScan("com.springboot.lottery") // 扫描配置有注解的包路径
public class LotterySpringBootStart extends SpringBootServletInitializer {
	// 获取jdbc.properties文件中的数据
	@Value("${spring.datasource.driver}")
	private String jdbcDriverClassName;
	@Value("${spring.datasource.url}") 
	private String jdbcUrl;
	@Value("${spring.datasource.username}")
	private String jdbcUserName;
	@Value("${spring.datasource.password}")
	private String jdbcPassword;

	/**
	 * 配置连接数据库的相关信息到数据源
	 * 
	 * @return 数据源
	 */
	@Bean
	public DataSource dataSource() {
		// 创建一个数据源的bean对象
		DruidDataSource dataSource = new DruidDataSource();
		// 设置连接驱动名
		dataSource.setDriverClassName(jdbcDriverClassName);
		// 设置连接数据库的地址
		dataSource.setUrl(jdbcUrl);
		// 设置连接数据库的用户名
		dataSource.setUsername(jdbcUserName);
		// 设置连接数据库的密码
		dataSource.setPassword(jdbcPassword);
		return dataSource;
	}

	/**
	 * 配置数据源到SqlSessionFactory的bean中
	 * 
	 * @return
	 */
	@Bean
	public SqlSessionFactoryBean sqlSessionFactory() {
		SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(this.dataSource());
		return sqlSessionFactory;
	}

	/**
	 * 配置外部tomcat
	 * 
	 * @param application
	 * @return
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(LotterySpringBootStart.class);
	}

	public static void main(String[] args) {
		// 启动服务器
		SpringApplication.run(LotterySpringBootStart.class, args);
	}
}
