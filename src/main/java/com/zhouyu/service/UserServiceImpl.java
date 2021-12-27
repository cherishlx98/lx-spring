package com.zhouyu.service;

import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;
import com.spring.InitializingBean;

/**
 * @Author: Xin Liu
 * @Date: 2021/12/25
 */
@Component("userService")
public class UserServiceImpl implements BeanNameAware, InitializingBean, UserService {

    @Autowired
    private OrderService orderService;

    // 不能通过"beanName"从spring容器中找到对应的bean，通过beanNameAware接口
    private String beanName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 可以在这个方法中验证属性是否赋值
        System.out.println("初始化");
    }

    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
        System.out.println("name: "+name);
    }
}
