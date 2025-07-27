package com.newzhxu;

import com.newzhxu.annotation.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zheng2580369@gmail.com
 */
public class ApplicationContext {
    private final Map<String, Object> ioc = new HashMap<>();
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    // 还未初始化完成的bean
    private final Map<String, Object> loadingIoc = new HashMap<>();

    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public ApplicationContext(String packageName) throws IOException {
        initContext(packageName);
    }

    public void initContext(String packageName) throws IOException {
        scanPackage(packageName).stream().filter(this::scanCreate).forEach(this::wrapper);
        initBeanPostProcessors();
        beanDefinitionMap.values().forEach(this::createBean);


    }

    private void initBeanPostProcessors() {
        this.beanDefinitionMap.values().stream()
                .filter(beanDefinition -> BeanPostProcessor.class.isAssignableFrom(beanDefinition.getBeanType()))
                .map(this::createBean)
                .map(bean -> (BeanPostProcessor) bean)
                .forEach(this.beanPostProcessors::add);
    }

    private BeanDefinition wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(type.getName())) {
            throw new RuntimeException("BeanDefinition already exists for " + type.getName());
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    protected boolean scanCreate(Class<?> type) {
        return type.isAnnotationPresent(Component.class);

    }

    protected Object createBean(BeanDefinition beanDefinition) {
        // 创建 Bean 实例
        // 这里可以使用反射来创建实例
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return ioc.get(name);
        }
        if (loadingIoc.containsKey(name)) {
            return loadingIoc.get(name);
        }
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean;
        try {
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            autowiredBean(bean, beanDefinition);
            bean = initializeBean(bean, beanDefinition);

            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    private Object initializeBean(Object bean, BeanDefinition beanDefinition) throws InvocationTargetException, IllegalAccessException {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.beforeInitializeBean(bean, beanDefinition.getName());
        }

        Method postConstructMethod = beanDefinition.getPostConstructMethod();
        if (postConstructMethod != null) {
            postConstructMethod.setAccessible(true);
            postConstructMethod.invoke(bean);
        }
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.postInitializeBean(bean, beanDefinition.getName());
        }
        return bean;
    }

    private void autowiredBean(Object bean, BeanDefinition beanDefinition) {
        beanDefinition.getAutowiredFields().forEach(field -> {
            field.setAccessible(true);
            try {
                field.set(bean, getBean(field.getType())); // 未初始化?
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<Class<?>> scanPackage(String packageName) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        // 扫描指定包下的类
        // 这里可以使用反射或其他方式来加载类
        // a.b.c -> a/b/c
        URL url = getClass().getClassLoader().getResource(packageName.replace(".", File.separator));
        Path path = Path.of(url.getFile());
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".class")) {
                    String replace = absolutePath.toString().replace(File.separator, ".");
                    int i = replace.indexOf(packageName);
                    String className = replace.substring(i, replace.length() - ".class".length());
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classes;
    }

    public Object getBean(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        Object o = this.ioc.get(name);
        if (o != null) {
            return o;
        }
        if (beanDefinitionMap.containsKey(name)) {
            return createBean(beanDefinitionMap.get(name));
        }


        return null;
    }

    public <T> T getBean(Class<T> beanType) {
        return this.beanDefinitionMap.values().stream().filter(bd -> beanType.isAssignableFrom(bd.getBeanType())).map(BeanDefinition::getName).findFirst()
                .map(e -> (T) getBean(e)).orElse(null);


    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return this.beanDefinitionMap.values().stream().filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .map(getBean -> (T) getBean(getBean)).toList();

    }


}
