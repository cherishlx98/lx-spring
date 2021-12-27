package com.spring;

/**
 * 初始化Bean接口
 *
 * @Author: Xin Liu
 * @Date: 2021/12/26
 */
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
