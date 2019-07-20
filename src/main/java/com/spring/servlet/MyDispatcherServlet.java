package com.spring.servlet;


import com.spring.annotation.MyAutowried;
import com.spring.annotation.MyController;
import com.spring.annotation.MyRepository;
import com.spring.annotation.MyService;
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
import java.lang.reflect.Field;
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

        //4、实现@UVAutowried自动注入
        doAutowried();

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
     * 将类名的首字母替换小写
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
