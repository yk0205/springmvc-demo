package com.spring.core;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @Description TODO
 * @Author yk
 * @Date 2019/7/20 19:37
 */

public class MethodHandler  {

    private Object object;
    private Method method;
    private List<String> params;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("object", object)
                .append("method", method)
                .append("params", params)
                .toString();
    }
}
