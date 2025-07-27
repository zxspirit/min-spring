package com.newzhxu.sub;

import com.newzhxu.annotation.Autowired;
import com.newzhxu.annotation.Component;
import com.newzhxu.annotation.PostConstruct;

/**
 * @author zheng2580369@gmail.com
 */
@Component
public class Cat {
    @Autowired
    private Head head;

    @PostConstruct
    public void init() {
        System.out.println("cat init" + head);
    }
}
