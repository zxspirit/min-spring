package com.newzhxu.web;

import com.newzhxu.annotation.Autowired;
import com.newzhxu.annotation.Component;
import com.newzhxu.annotation.PostConstruct;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.logging.LogManager;

/**
 * @author zheng2580369@gmail.com
 */
@Component
public class TomcatServer {
    private final Logger log = LoggerFactory.getLogger(TomcatServer.class);

    @Autowired
    private DispatcherServlet dispatcherServlet;

    @PostConstruct
    public void start() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        int port = 8080;
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File("").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);

        tomcat.addServlet(contextPath, "dispatcherServlet", dispatcherServlet);
        context.addServletMappingDecoded("/*", "dispatcherServlet");


        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }
}
