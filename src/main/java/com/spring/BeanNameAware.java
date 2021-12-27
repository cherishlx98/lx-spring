package com.spring;

/**
 * Aware回调接口
 *
 * @Author: Xin Liu
 * @Date: 2021/12/26
 */
public interface BeanNameAware {

    /**
     * 设置bean的beanName属性
     *
     * @param beanName
     */
    void setBeanName(String beanName);
}
