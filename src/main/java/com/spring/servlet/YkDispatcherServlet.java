package com.spring.servlet;



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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 *
 */
public class YkDispatcherServlet extends HttpServlet {

    private final static Logger logger = Logger.getLogger(YkDispatcherServlet.class);

    //spring配置文件
    private Properties properties = new Properties();

    //存放所有带注解的类
    private List<String> classNameList = new ArrayList<>();

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
        doScanner(properties.getProperty("basepackage"));





        logger.info("logger info servlet初始化完成");

    }

    /**
     * 1、加载配置
     */
    public void doLoadConfig() {

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
     * @param path
     */
    public void doScanner(String path) {

        if (path.endsWith(".class")) {
            //获取到带有包路径的类名
            String className = path.substring(0, path.lastIndexOf(".class"));
            //扫描的类
            classNameList.add(className);
        }else{
            URL url = this.getClass().getClassLoader().getResource("/"+ path.replaceAll("\\.", "/"));
            //是包路径，继续迭代
            File file = new File(url.getFile());
            File[] files = file.listFiles();
            for (File f : files) {
                doScanner(path + "." + f.getName());
            }
        }
    }
}
