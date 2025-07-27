package com.newzhxu.sub;

import com.newzhxu.annotation.Autowired;
import com.newzhxu.annotation.Component;
import com.newzhxu.annotation.PostConstruct;

/**
 * @author zheng2580369@gmail.com
 */
@Component(name = "myDog")
public class Dog {
    @Autowired
    private Head head;
    @Autowired
    private Dog dog;

    @PostConstruct

    public void init() {

        System.out.println(this.head + " dog init" + dog);
    }


}
