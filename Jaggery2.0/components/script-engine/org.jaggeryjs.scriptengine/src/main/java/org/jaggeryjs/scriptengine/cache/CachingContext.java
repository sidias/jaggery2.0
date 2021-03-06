package org.jaggeryjs.scriptengine.cache;

public class CachingContext {
    private String tenantId = null;
    private String context = null;
    private String path = null;
    private String cacheKey = null;
    private long lastUsageTime = 0L;
    private long lastModificationTest = 0L;
    private long cacheUpdatedTime = 0L;
    private long sourceModifiedTime = 0L;
    private String className = null;

    public CachingContext(String context, String path, String cacheKey) {
        this.cacheKey = cacheKey;
        this.context = context;
        this.path = path;
    }


    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public long getLastUsageTime() {
        return lastUsageTime;
    }

    public void setLastUsageTime(long lastUsageTime) {
        this.lastUsageTime = lastUsageTime;
    }

    public long getLastModificationTest() {
        return lastModificationTest;
    }

    public void setLastModificationTest(long lastModificationTest) {
        this.lastModificationTest = lastModificationTest;
    }

    public long getCacheUpdatedTime() {
        return cacheUpdatedTime;
    }

    public void setCacheUpdatedTime(long cacheUpdatedTime) {
        this.cacheUpdatedTime = cacheUpdatedTime;
    }

    public long getSourceModifiedTime() {
        return sourceModifiedTime;
    }

    public void setSourceModifiedTime(long sourceModifiedTime) {
        this.sourceModifiedTime = sourceModifiedTime;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
