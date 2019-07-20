package com.spring.servlet;


import com.spring.annotation.*;

import com.spring.core.MethodHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodParameterNamesScanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * dispatcherServlet
 */
public class MyDispatcherServlet extends HttpServlet {

    private final static Logger logger = Logger.getLogger(MyDispatcherServlet.class);

    //spring配置文件
    private Properties properties = new Properties();

    //存放所有带注解的类
    private List<String> classNameList = new ArrayList<>();

    //当通过类型找不到对应实例时，通过名称注入(名称相同时会覆盖之前的值，这里就不处理了)
    private Map<String, Object> IOCByName = new HashMap<>();

    //IOC容器,通过类型注入
    private Map<String, Object> IOCByType = new HashMap<>();

    //url 到controller方法的映射
    private Map<String, MethodHandler> urlHandler = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6、处理请求，执行相应的方法
        doHandler(req, resp);
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

        //4、实现@UVAutowried自动注入
        doAutowried();

        //5、初始化HandlerMapping，根据url映射不同的controller方法
        doMapping();

        //6、处理请求，执行相应的方法
        doHandler();
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
                // 2.使用用户自定义名，如 @MyService("abc")
                // 3.如果service实现了接口，可以使用接口作为key
                String beanName = null;
                boolean needNewInstanse = false;

                //放入MyController注解
                if (clazz.isAnnotationPresent(MyController.class)) {
                    MyController controller = clazz.getAnnotation(MyController.class);
                    beanName = controller.value().trim();
                    needNewInstanse = true;
                }
                // 放入MyService注解类
                if (clazz.isAnnotationPresent(MyService.class)) {
                    MyService service = clazz.getAnnotation(MyService.class);
                    beanName = service.value().trim();
                    needNewInstanse = true;
                }
                // 放入MyRepository注解类
                if (clazz.isAnnotationPresent(MyRepository.class)) {
                    MyRepository repository = clazz.getAnnotation(MyRepository.class);
                    beanName = repository.value().trim();
                    needNewInstanse = true;
                }
                //如果用户没有定义名称，使用名首字母小写
                if (StringUtils.isBlank(beanName)) {
                    beanName = lowerFirstCase(clazz.getSimpleName());
                }
                // 创建实例
                if (needNewInstanse) {
                    //byName
                    Object instance = clazz.newInstance();
                    IOCByName.put(beanName, instance);
                    //byType
                    IOCByType.put(clazz.getName(), instance);
                    // 如果接口的情况
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
     * 4、实现autowire自动注入
     */
    private void doAutowried() {
        if (IOCByName.isEmpty() && IOCByType.isEmpty()) {
            return;
        }
        // 遍历ioc容器类型
        IOCByType.entrySet().forEach(entry -> {
            //获取变量
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                //private、protected修饰的变量可访问
                field.setAccessible(true);
                if (!field.isAnnotationPresent(MyAutowried.class)) {
                    continue;
                }
                Object instance = null;
                String beanTypeName = field.getType().getName();
                String simpleName = lowerFirstCase(field.getType().getSimpleName());
                //首先根据Type注入，没有实例时根据Name，否则抛出异常
                if (IOCByType.containsKey(beanTypeName)) {
                    instance = IOCByType.get(beanTypeName);
                } else if (IOCByName.containsKey(simpleName)) {
                    instance = IOCByName.get(simpleName);
                } else {
                    throw new RuntimeException("not find class to autowire");
                }
                try {
                    //向obj对象的这个Field设置新值value,依赖注入
                    field.set(entry.getValue(), instance);
                } catch (IllegalAccessException e) {
                    if (logger.isInfoEnabled()) {
                        logger.error("IllegalAccessException error  ", e);
                    }
                }
            }
        });
    }

    /**
     * 5、初始化HandlerMapping，根据url映射不同的controller方法
     */
    private void doMapping() {
        if (IOCByType.isEmpty() && IOCByName.isEmpty()) {
            return;
        }

        IOCByType.entrySet().forEach(entry -> {
            Class<?> clazz = entry.getValue().getClass();
            //判断是否是controller
            if (!clazz.isAnnotationPresent(MyController.class)) {
                return;
            }
            String startUrl = "/";
            //判断controller类上是否有MyRequestMapping注解，如果有则拼接url
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                String value = requestMapping.value();
                if (!StringUtils.isBlank(value)) {
                    startUrl += value;
                }
                //遍历controller类中UVRequestMapping注解修饰的方法，添加到urlHandler中,完成url到方法的映射
                Method[] methods = clazz.getDeclaredMethods();
                // 拿到controller下所有的方法
                for (Method method : methods) {
                    // 判断方法上是否有requestMapping注解
                    if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                        continue;
                    }
                    MyRequestMapping annotation = method.getAnnotation(MyRequestMapping.class);
                    String url = startUrl + "/" +annotation.value().trim();
                    //解决多个/重叠的问题
                    url = url.replaceAll("/+", "/");
                    MethodHandler handler = new MethodHandler();
                    //放入方法
                    handler.setMethod(method);
                    try {
                        //放入方法所在的controller
                        handler.setObject(entry.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //放入方法的参数列表
                    List<String> params = doParamHandler(method);
                    handler.setParams(params);
                    urlHandler.put(url, handler);
                }
            }
        });
    }


    /**
     * 6、处理请求，执行相应的方法
     */
    private void doHandler(HttpServletRequest request, HttpServletResponse response) {



    }





































    /**
     *
     * @param method
     * @return
     */
    /**
     * 在Java 8之前的版本，代码编译为class文件后， 方法参数的类型是固定的，但参数名称却丢失了， 这和动态语言严重依赖参数名称形成了鲜明对比。
     * 现在Java 8开始在class文件中保留参数名，给反射带来了极大的便利。
     * 使用reflections包，jdk7和jdk8都可用
     **/
    //处理method的参数
    private List<String> doParamHandler(Method method) {
        //使用reflections进行参数名的获取
        Reflections reflections = new Reflections(new MethodParameterNamesScanner());
        //参数名与顺序对应
        List<String> paramNames = reflections.getMethodParamNames(method);
        return paramNames;
    }

    /**
     * 将类名的首字母替换小写
     * @param str
     * @return
     */
    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        //ascii码计算
        chars[0] += 32;
        return String.valueOf(chars);
    }


}
