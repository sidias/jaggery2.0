package org.jaggeryjs.scriptengine.engine;

import jdk.nashorn.internal.runtime.ScriptObject;

import java.util.HashMap;
import java.util.Map;

public class JaggeryContext {

    private String tenantId = null;
    private NashornEngine engine = null;
    private ScriptObject scope = null;

    private Map<String, Object> properties = new HashMap<String, Object>();

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public NashornEngine getEngine() {
        return engine;
    }

    public void setEngine(NashornEngine engine) {
        this.engine = engine;
    }

    public ScriptObject getScope() {
        return scope;
    }

    public void setScope(ScriptObject scope) {
        this.scope = scope;
    }

    public void addProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}

