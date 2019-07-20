package com.spring.servlet;


import com.spring.annotation.YKController;
import com.spring.annotation.YKService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * dispatcherServlet
 */
public class YkDispatcherServlet extends HttpServlet {

    private final static Logger logger = Logger.getLogger(YkDispatcherServlet.class);

    //spring配置文件
    private Properties properties = new Properties();

    //存放所有带注解的类
    private List<String> classNameList = new ArrayList<>();

    //当通过类型找不到对应实例时，通过名称注入(名称相同时会覆盖之前的值，这里就不处理了)
    private Map<String, Object> IOCByName = new ConcurrentHashMap<>();

    //IOC容器,通过类型注入
    private Map<String, Object> IOCByType = new ConcurrentHashMap<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    public void init() throws ServletException {
        logger.info("logger info servlet开始初始化");

        //1、加载配置文件 spring-config.properties,获取扫描路径
        doLoadConfig();

        //2、扫描配置的路径下的带有注解的类
        doScannerClass(properties.getProperty("basepackage"));

        //3、初始化所有的类，被放入到IOC容器中
        doPutIOC();
        logger.info("logger info servlet初始化完成");
    }

    /**
     * 1、加载配置
     */
    private void doLoadConfig() {

        try {
            ServletConfig config = this.getServletConfig();
            String configLocation = config.getInitParameter("contextConfigLocation");
            if (configLocation.contains(":")) {
                // 获取的是classpath:spring-config.properties 或 classpath*:spring-config.properties需要截取
                configLocation = configLocation.split(":")[1];
            }
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation);
            properties.load(inputStream);
            if (logger.isDebugEnabled()) {
                logger.info("doLoadConfig finish ");
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.error("doLoadConfig error  ", e);
            }
        }
    }

    /**
     * 2、扫描配置的路径下的带有注解的类
     *
     * @param path 类路径
     */
    private void doScannerClass(String path) {

        if (path.endsWith(".class")) {
            //获取到带有包路径的类名
            String className = path.substring(0, path.lastIndexOf(".class"));
            //扫描的类
            classNameList.add(className);
        } else {
            URL url = this.getClass().getClassLoader().getResource("/" + path.replaceAll("\\.", "/"));
            //是包路径，继续迭代
            File file = new File(url.getFile());
            File[] files = file.listFiles();
            for (File f : files) {
                doScannerClass(path + "." + f.getName());
            }
        }
    }

    /**
     * 3、初始化所有的类，被放入到IOC容器中
     */
    private void doPutIOC() {
        if (classNameList.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.error("classNameList isEmpty ");
            }
            return;
        }
        try {
            for (String className : classNameList) {
                //反射获取实例对象
                Class<?> clazz = Class.forName(className);
                //IOC容器key命名规则：
                // 1.默认类名首字母小写
                // 2.使用用户自定义名，如 @UVService("abc")
                // 3.如果service实现了接口，可以使用接口作为key
                //controller,service注解类
                if (clazz.isAnnotationPresent(YKController.class)) {
                    YKController controller = clazz.getAnnotation(YKController.class);
                    String beanName = controller.value().trim();
                    //如果用户没有定义名称，使用名首字母小写
                    if (StringUtils.isBlank(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    //byName
                    Object instance = clazz.newInstance();
                    IOCByName.put(beanName, instance);
                    //byType
                    IOCByType.put(clazz.getName(), instance);
                }

                if (clazz.isAnnotationPresent(YKService.class)) {
                    YKService service = clazz.getAnnotation(YKService.class);
                    String beanName = service.value().trim();
                    //如果用户没有定义名称，使用名首字母小写
                    if (StringUtils.isBlank(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    //byName
                    Object instance = clazz.newInstance();
                    IOCByName.put(beanName, instance);
                    //byType
                    IOCByType.put(clazz.getName(), instance);
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> interf : interfaces) {
                        IOCByName.put(lowerFirstCase(interf.getSimpleName()), instance);
                        IOCByType.put(interf.getName(), instance);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            if (logger.isInfoEnabled()) {
                logger.error("ClassNotFoundException error  ", e);
            }
        } catch (InstantiationException e) {
            if (logger.isInfoEnabled()) {
                logger.error("InstantiationException error  ", e);
            }
        } catch (IllegalAccessException e) {
            if (logger.isInfoEnabled()) {
                logger.error("IllegalAccessException error  ", e);
            }
        }
    }


    /**
     * 将类名的首字母小写
     *
     * @param name
     * @return
     */
    private String lowerFirstCase(String name) {
        String firstChar = name.substring(0, 1);
        String lowerChar = firstChar.toLowerCase();
        return name.replaceFirst(firstChar, lowerChar);
    }


}
