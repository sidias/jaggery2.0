package org.jaggeryjs.scriptengine.engine;
//no need of this class.nashorn has ScriptRuntime.property class

import jdk.nashorn.internal.runtime.Property;

public class JavascriptProperty {

    private String name = null;
    private Object value = null;
    private int attribute = Property.NOT_ENUMERABLE;

    public JavascriptProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public int getAttribute() {
        return attribute;
    }
}
