package com.newzhxu;

import com.newzhxu.sub.Cat;
import com.newzhxu.sub.Dog;

/**
 * @author zheng2580369@gmail.com
 */
public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ApplicationContext("com.newzhxu");
        Cat bean = (Cat) context.getBean("Cat");
        System.out.println(bean);
        Dog myDog = (Dog) context.getBean("myDog");
        System.out.println(myDog);

    }
}
