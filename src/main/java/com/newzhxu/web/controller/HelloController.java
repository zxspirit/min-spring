package com.newzhxu.web.controller;

import com.newzhxu.annotation.Component;
import com.newzhxu.web.*;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Map;

/**
 * @author zheng2580369@gmail.com
 */
@Controller
@RequestMapping("/hello")
@Component
public class HelloController {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(HelloController.class);

    @RequestMapping("/a")
    public String hello(@Param("aa") String a, @Param("bb") Integer b, @Param("cc") String[] c) {
        logger.info("hello{}{}{}", a, b, c);
        return "hello " + a + " " + b + " " + Arrays.toString(c);
    }

    @RequestMapping("/json")
    @ResponseBody
    public User json(@Param("age") Integer age, @Param("name") String name) {
        logger.info("json age:{}, name:{}", age, name);
        User user = new User();
        user.setName(name);
        user.setAge(age);
        return user;
    }

    @RequestMapping("/module")
    public ModelAndView modelAndView() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index.html");
        Map<String, String> context = modelAndView.getContext();
        context.put("name", "aaa");
        return modelAndView;
    }


}
