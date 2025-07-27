package com.newzhxu.web;

import java.lang.reflect.Method;

/**
 * @author zheng2580369@gmail.com
 */
public class WebHandler {

    private final Object controllerBean;
    private final Method method;

    private final ResultType resultType;

    public WebHandler(Object controller, Method method) {
        this.controllerBean = controller;
        this.method = method;
        this.resultType = resoveResultType(controller, method);
    }

    public ResultType getResultType() {
        return resultType;
    }

    private ResultType resoveResultType(Object controller, Method method) {
        if (method.isAnnotationPresent(ResponseBody.class)) {
            return ResultType.JSON;
        }
        if (method.getReturnType() == ModelAndView.class) {
            return ResultType.LOCAL;
        }
        return ResultType.HTML;
    }

    public Object getControllerBean() {
        return controllerBean;
    }

    public Method getMethod() {
        return method;
    }

    enum ResultType {
        HTML, JSON, LOCAL
    }
}
