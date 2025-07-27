package com.newzhxu.web;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zheng2580369@gmail.com
 */
public class ModelAndView {
    private String viewName;
    private final Map<String, String> context = new HashMap<>();

    public String getViewName() {
        return viewName;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
