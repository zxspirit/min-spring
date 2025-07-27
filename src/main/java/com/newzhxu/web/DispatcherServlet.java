package com.newzhxu.web;

import com.google.gson.Gson;
import com.newzhxu.BeanPostProcessor;
import com.newzhxu.annotation.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zheng2580369@gmail.com
 */
@Component
public class DispatcherServlet extends HttpServlet implements BeanPostProcessor {
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(.*?)}");
    private final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
    private final Map<String, WebHandler> handlerMap = new HashMap<>();

    @Override
    public Object postInitializeBean(Object bean, String beanName) {
        if (!bean.getClass().isAnnotationPresent(Controller.class)) {
            return bean;
        }
        String classUrl;
        RequestMapping requestMappingAnnotation = bean.getClass().getAnnotation(RequestMapping.class);
        if (requestMappingAnnotation != null) {
            classUrl = requestMappingAnnotation.value();
        } else {
            classUrl = "";
        }
        Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(method -> {
                    String methodUrl = method.getAnnotation(RequestMapping.class).value();
                    String fullUrl = classUrl + methodUrl;
                    WebHandler handler = new WebHandler(bean, method);
                    if (handlerMap.put(fullUrl, handler) != null) {
                        throw new RuntimeException("Duplicate mapping: " + fullUrl);
                    }
                });
        return bean;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebHandler handler = findController(req);
        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found");
            logger.warn("No handler found for request: {}", req.getRequestURI());
            return;
        }
        Object controllerBean = handler.getControllerBean();
        Method method = handler.getMethod();
        Map<String, String[]> parameterMap = req.getParameterMap();
        Object[] args = resoveArgs(method, parameterMap);
        Object result;
        try {
            result = method.invoke(controllerBean, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        switch (handler.getResultType()) {
            case JSON -> {
                resp.setContentType("application/json");
                resp.getWriter().write(new Gson().toJson(result));
            }
            case HTML -> {
                resp.setContentType("text/html");
                resp.getWriter().write(result.toString());
            }
            case LOCAL -> {
                ModelAndView modelAndView = (ModelAndView) result;

                String viewName = modelAndView.getViewName();
                InputStream stream = this.getClass().getClassLoader().getResourceAsStream(viewName);
                if (stream == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("404 Not Found: " + viewName);
                    logger.warn("View not found: {}", viewName);
                    return;
                }
                try (stream) {
                    String html = new String(stream.readAllBytes());
                    Map<String, String> context = modelAndView.getContext();
                    html = renderTemplate(html, context);
                    resp.setContentType("text/html;charset=UTF-8");
                    resp.getWriter().write(html);
                }
            }
        }
    }

    private String renderTemplate(String html, Map<String, String> context) {
        Matcher matcher = PATTERN.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String group = matcher.group(1);
            String value = context.getOrDefault(group, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();

    }

    private Object[] resoveArgs(Method method, Map<String, String[]> parameterMap) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[method.getParameterCount()];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Param annotation = parameter.getAnnotation(Param.class);
            String name;
            if (annotation != null) {
                name = annotation.value();
            } else {
                name = parameter.getName();

            }

            String[] strings = parameterMap.get(name);
            if (strings == null || strings.length == 0) {
                args[i] = null;
                continue;
            }
            Class<?> type = parameter.getType();
            if (String.class.isAssignableFrom(type)) {
                args[i] = strings[0];
            } else if (Integer.class.isAssignableFrom(type)) {
                args[i] = Integer.parseInt(strings[0]);
            } else if (String[].class.isAssignableFrom(type)) {
                args[i] = strings;
            }
        }


        return args;
    }

    private WebHandler findController(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        return handlerMap.get(requestURI);
    }
}
