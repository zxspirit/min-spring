package com.newzhxu;

import com.newzhxu.annotation.Autowired;
import com.newzhxu.annotation.Component;
import com.newzhxu.annotation.PostConstruct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author zheng2580369@gmail.com
 */
public class BeanDefinition {
    private final String beanName;
    private final Constructor<?> constructor;
    private final Method postConstructMethod;
    private final List<Field> autowiredFields;
    private final Class<?> beanType;

    public BeanDefinition(Class<?> type) {
        this.beanType = type;
        Component component = type.getDeclaredAnnotation(Component.class);
        this.beanName = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            this.constructor = type.getConstructor();
            List<Method> list = Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(PostConstruct.class)).toList();
            if (list.size() > 1) {
                throw new RuntimeException("Bean " + type.getName() + " has more than one @PostConstruct method");
            }
            this.postConstructMethod = list.isEmpty() ? null : list.getFirst();
            this.autowiredFields = Arrays.stream(type.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Autowired.class)).toList();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    public String getName() {
        return this.beanName;
    }

    public Constructor<?> getConstructor() {

        return constructor;
    }

    public Method getPostConstructMethod() {
        return postConstructMethod;
    }

    public List<Field> getAutowiredFields() {
        return autowiredFields;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

}
