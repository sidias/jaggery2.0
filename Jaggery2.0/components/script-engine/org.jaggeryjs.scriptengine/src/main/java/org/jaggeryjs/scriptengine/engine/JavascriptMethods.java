package org.jaggeryjs.scriptengine.engine;

import jdk.nashorn.internal.objects.annotations.Function;
import jdk.nashorn.internal.runtime.Context;
import org.jaggeryjs.scriptengine.exception.ScriptException;

import java.lang.reflect.Method;

public class JavascriptMethods {

    private String name = null;
    private Class clazz = null;
    private Method method = null;
    private String methodName = null;
    private int attribute = 4;

    public JavascriptMethods(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Method getMethod() throws ScriptException{
        if(method != null) {
            return method;
        }

        try{
            method = clazz.getDeclaredMethod(
                    //wrong here
                    name, Context.class, Object.class, Object[].class, Function.class
            );
            return method;
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }
}
