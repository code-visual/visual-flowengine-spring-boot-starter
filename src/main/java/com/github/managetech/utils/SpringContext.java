package com.github.managetech.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 获取Spring容器中bean工具类
 * @author ZENG.XIAO.YAN
 * @date   2018年6月6日
 * 
 */
@Component
public class SpringContext implements ApplicationContextAware {

	private static ApplicationContext applicationContext = null;

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String beanId) {
		return (T) applicationContext.getBean(beanId);
	}

	public static <T> T getBean(Class<T> requiredType) {
		return (T) applicationContext.getBean(requiredType);
	}

	/**
	 * Spring容器启动后，会把 applicationContext 给自动注入进来，然后我们把 applicationContext 赋值到静态变量中，方便后续拿到容器对象
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContext.applicationContext = applicationContext;
	}

}