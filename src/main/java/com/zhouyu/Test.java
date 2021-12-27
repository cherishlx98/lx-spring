package com.zhouyu;

import com.spring.ZhouyuApplicationContext;
import com.zhouyu.service.UserService;

/**
 * @Author: Xin Liu
 * @Date: 2021/12/25
 */
public class Test {
    public static void main(String[] args) {
        ZhouyuApplicationContext applicationContext = new ZhouyuApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}
