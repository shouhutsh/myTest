package com.glbpay.common.util.test;

import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单模拟Junit测试框架，为了避免 maven 编译时由于单元测试错误而导致失败
 *      需要实现继承子类，并在 main 方法中调用 simpleJunit
 */
public abstract class BaseJunit4Test {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    protected @interface Before{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    protected @interface Test{}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    protected @interface After{}

    protected void simpleJunit() {
        doAutoWired(this);

        Method[] methods = this.getClass().getDeclaredMethods();

        LinkedHashMap<Class, List<Method>> map = new LinkedHashMap<>();
        map.put(Before.class, new ArrayList<>());
        map.put(Test.class, new ArrayList<>());
        map.put(After.class, new ArrayList<>());

        for (Method m : methods) {
            next:
            for (Map.Entry<Class, List<Method>> e : map.entrySet()) {
                for (Annotation a : m.getDeclaredAnnotations()) {
                    if (e.getKey() == a.annotationType()) {
                        e.getValue().add(m);
                        break next;
                    }
                }
            }
        }

        for (Map.Entry<Class, List<Method>> e : map.entrySet()) {
            for (Method m : e.getValue()) {
                try {
                    m.invoke(this);
                    System.out.println("TEST SUCCESS: " + this.getClass().getSimpleName() + "." + m.getName());
                } catch (Exception e1) {
                    System.out.println("TEST FAILED: " + m.getName());
                }
            }
        }
    }

    private void doAutoWired(Object object){
        try {
            List<Field> needAutoWiredFileds = getAllNeedAutoWiredFileds(this.getClass());
            ApplicationContext context = getApplicationContext(this);
            for (Field f : needAutoWiredFileds) {
                if (null == f.get(object)) {
                    f.set(object, context.getBean(f.getType()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Field> getAllNeedAutoWiredFileds(Class clazz) throws IllegalAccessException {
        if(null == clazz || clazz == BaseJunit4Test.class) return ListUtils.EMPTY_LIST;

        List<Field> needAutoWiredFileds = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            if (null != f.getAnnotation(Autowired.class)) {
                f.setAccessible(true);
                needAutoWiredFileds.add(f);
            }
        }
        needAutoWiredFileds.addAll(getAllNeedAutoWiredFileds(clazz.getSuperclass()));
        return needAutoWiredFileds;
    }

    private ApplicationContext getApplicationContext(Object object) {
        ApplicationContext context = null;
        ContextConfiguration configuration = object.getClass().getAnnotation(ContextConfiguration.class);
        if (null != configuration) {
            for (String path : configuration.locations()) {
                if (null == configuration) {
                    context = new ClassPathXmlApplicationContext(path);
                } else {
                    context = new ClassPathXmlApplicationContext(new String[]{path}, context);
                }
            }
        }
        return context;
    }
}
