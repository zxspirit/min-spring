package com.newzhxu;

import com.newzhxu.annotation.Component;

/**
 * @author zheng2580369@gmail.com
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {


    @Override
    public Object postInitializeBean(Object bean, String beanName) {
        System.out.println(beanName + "初始化完成了");
        return bean;
    }
}
