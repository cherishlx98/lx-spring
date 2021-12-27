package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Xin Liu
 * @Date: 2021/12/25
 */
public class ZhouyuApplicationContext {
    // 配置类的class
    private Class configClass;

    // 单例池，存放单例bean Map<beanName,bean对象>
    private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();

    // beanDefinitionMap 存放的是spring启动的时候 扫描 到的beanDefintion
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    // 存放beanPostProcessor
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ZhouyuApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类
        // 解析@ComponentScan-->扫描路径-->扫描--->beanDefinition--->BeanDefinitionMap
        scan(configClass);

        // 在spring容器启动的时候，将单例bean加载到spring容器中
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = createBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }
    }

    /**
     * 单例、原型bean都是通过这个方法创建的
     * @param beanDefinition
     * @return
     */
    public Object createBean(String beanName,BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            // 实例化
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 自动注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    // 简单通过属性的名字从spring容器中找出bean对象
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance,bean);
                }
            }

            // Aware回调
            // 实现了BeanNameAware接口，调用setBeanName方法
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // postProcessorBeforeInitialization 初始化前方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化逻辑，实现InitializingBean接口
            if (instance instanceof InitializingBean) {
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // postProcessorAfterInitialization 初始化后方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                // instance引用可能改变，可能返回代理对象的引用
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation =(ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();//com.zhouyu.service

        // 扫描 通过类加载器去加载class到JVM中
        // Bootstrap--->jre/lib
        // Ext--------->jre/ext/lib
        // App--------->classpath
        ClassLoader classLoader = ZhouyuApplicationContext.class.getClassLoader();
        // 类加载器去加载相对路径的资源
        URL resource = classLoader.getResource(path.replaceAll("\\.", "/"));
//        System.out.println(resource.getFile());
        // 转换成File，好操作
        File file = new File(resource.getFile());

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                ///Users/liuxin/Desktop/Demo/lx-spring/target/classes/com/zhouyu/service/UserService.class
                String absolutePath = file1.getAbsolutePath();
                if (absolutePath.endsWith(".class")) {
                    String substring = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));

                    // 获取类的全限定名
                    String className = substring.replaceAll("/", "\\.");//com.zhouyu.service.UserService

                    try {
                        Class<?> clazz = classLoader.loadClass(className);

                        // 如果类上面加了@Component，则生成bean到spring容器
                        if (clazz.isAnnotationPresent(Component.class)) {

                            // 判断是否是BeanPostProcessor
                            //父类.class.isAssignableFrom(子类.class)
                            //
                            //子类实例 instanceof 父类类型
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }

                            // TODO beanPostProcessor也会走下面的逻辑，在spring中不是这么实现的

                            // 根据bean的作用域判断是否需要生成bean注入到spring容器
                            // 解析类 -> BeanDefinition
                            // BeanDefinition 有一个属性表示bean是单例还是原型
                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            BeanDefinition beanDefinition = new BeanDefinition();
                            // 设置clazz
                            beanDefinition.setClazz(clazz);
                            // 设置scope
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                // 默认设置bean为单例
                                beanDefinition.setScope("singleton");
                            }
                            // 添加beanDefinition到beanDefinitionMap中
                            beanDefinitionMap.put(beanName,beanDefinition);

                        }
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                Object o = singletonObjects.get(beanName);
                return o;
            } else {
                // 原型，创建bean返回
                return createBean(beanName,beanDefinition);
            }
        } else {
            throw new RuntimeException("不存在的beanName");
        }
    }
}
