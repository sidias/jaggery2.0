package org.jaggeryjs.scriptengine.engine;
//no need of this class.nashorn has ScriptRuntime.property class

public class JavaScriptProperty {

    private String name = null;
    private Object value = null;
    private int attribute = 4;

    public JavaScriptProperty(String name) {
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

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }
}
