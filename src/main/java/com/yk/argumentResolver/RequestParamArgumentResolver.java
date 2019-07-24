package com.yk.argumentResolver;


import com.spring.annotation.MyRequestParam;
import com.spring.annotation.MyService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @Description TODO
 * @Author yk
 * @Date 2019/7/22 20:58
 */
@MyService("requestParamArgumentResolver")
public class RequestParamArgumentResolver implements ArgumentResolver {

    @Override
    public boolean support(Class<?> type, int paramIndex, Method method) {
        //获取当前方法的参数
        Annotation[][] an = method.getParameterAnnotations();
        Annotation[] paramAns = an[paramIndex];

        for (Annotation paramAn : paramAns) {
            //判断传进的paramAn.getClass()是不是 CustomRequestParam 类型
            if (MyRequestParam.class.isAssignableFrom(paramAn.getClass())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object argumentResolver(HttpServletRequest request,
                                   HttpServletResponse response, Class<?> type, int paramIndex,
                                   Method method) {

        //获取当前方法的参数
        Annotation[][] an = method.getParameterAnnotations();
        Annotation[] paramAns = an[paramIndex];

        for (Annotation paramAn : paramAns) {
            //判断传进的paramAn.getClass()是不是 CustomRequestParam 类型
            if (MyRequestParam.class.isAssignableFrom(paramAn.getClass())) {
                MyRequestParam cr = (MyRequestParam) paramAn;
                String value = cr.value();
                switch (type.getSimpleName()) {
                    case "String":
                        return request.getParameter(value);
                    case "Integer":
                        return Integer.valueOf(request.getParameter(value));
                    case "int":
                        return Integer.valueOf(request.getParameter(value));
                    case "long":
                        return Long.valueOf(request.getParameter(value));
                    case "Long":
                        return Long.valueOf(request.getParameter(value));
                    default:
                        return request.getParameter(value);
                }
            }
        }
        return null;
    }

}
