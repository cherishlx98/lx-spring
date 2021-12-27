package com.spring;

/**
 * @Author: Xin Liu
 * @Date: 2021/12/26
 */
public interface BeanPostProcessor {
    /**
     * 初始化前执行该方法
     * @param bean
     * @param beanName
     * @return
     */
    Object postProcessBeforeInitialization(Object bean, String beanName);

    /**
     * 初始化后执行该方法
     * @param bean
     * @param beanName
     * @return
     */
    Object postProcessAfterInitialization(Object bean, String beanName);
}
