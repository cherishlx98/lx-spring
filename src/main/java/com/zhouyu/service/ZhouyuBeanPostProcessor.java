package com.zhouyu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: Xin Liu
 * @Date: 2021/12/26
 */
@Component
public class ZhouyuBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if ("userService".equals(beanName)) {
            System.out.println("初始化前");
            ((UserServiceImpl) bean).setName("周瑜好帅");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        if ("userService".equals(beanName)) {
            Object proxyInstance = Proxy.newProxyInstance(ZhouyuBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑"); // aop找切点pointCut：代理逻辑定义在哪些类上
                    return method.invoke(bean,args); // 执行target对象的方法
                }
            });

            // 直接返回代理对象
            return proxyInstance;
        }

        // 返回原始对象
        return bean;
    }
}
