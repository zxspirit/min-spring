package com.newzhxu;

/**
 * @author zheng2580369@gmail.com
 */
public interface BeanPostProcessor {
    default Object beforeInitializeBean(Object bean, String beanName) {
        return bean;
    }

    default Object postInitializeBean(Object bean, String beanName) {
        return bean;
    }
}
