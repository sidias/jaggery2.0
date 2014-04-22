package org.jaggeryjs.scriptengine.cache;

import jdk.nashorn.internal.runtime.ScriptEnvironment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by buddhi on 4/9/14.
 */
public class CacheManager {

    private static final Log log = LogFactory.getLog(CacheManager.class);

    private final Object lock = new Object();

    public CacheManager(ScriptEnvironment env) {

    }
}
