package org.jaggeryjs.jaggery.core.manager;

import jdk.nashorn.internal.runtime.*;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.jaggeryjs.scriptengine.parser.JaggeryParser;
import org.jaggeryjs.scriptengine.EngineConstants;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.NashornEngine;
import org.jaggeryjs.scriptengine.exception.ScriptException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CommonManager {

    private static final Log log = LogFactory.getLog(CommonManager.class);

    public static final String JAGGERY_URLS_MAP = "jaggery.urls.map";
    public static final String JAGGERY_OUTPUT_STREAM = "jaggery.output.stream";

    private static final String JAGGERY_CORE_LIB = "resources/libs/";

    private static CommonManager manager;
    private NashornEngine engine = null;
    //----------------set this module dir as a global property to current global.
    //then scripts can jaggery.js file can access that property.
    private String moduleDir;

    private CommonManager() throws ScriptException {
    }

    public static CommonManager getInstance() throws ScriptException {
        if (manager == null) {
            manager = new CommonManager();
        }
        return manager;
    }

    public NashornEngine getEngine() {
        return this.engine;
    }


    public void initialize(String moduleDir) throws ScriptException, PrivilegedActionException, IOException {
        this.engine = nashornEngineInstance();
        this.moduleDir = moduleDir;

        //read core modules and set them as global property.
        //meka wenathanaka karanna.
       // corelibUpdate();
    }

    /**
     * Create a new nashornEngine initialized with a CacheManager.
     *
     * @return newly created NashornEngine.
     * */
    private NashornEngine nashornEngineInstance() throws PrivilegedActionException, IOException, ScriptException {
        checkConfigPermission();
        return new NashornEngine(new CacheManager(null), getAppClassLoader());
    }

    public static void initContext(JaggeryContext context) throws
            ScriptException {
        context.setEngine(manager.engine);
        context.setScope(manager.engine.getRuntimeScope());
        //context.setTenantId(Integer.toString());

        context.addProperty(JaggeryCoreConstants.JAGGERY_CORE_MANAGER, manager);
        context.addProperty(JaggeryCoreConstants.JAGGERY_INCLUDED_SCRIPTS, new HashMap<String, Boolean>());
        context.addProperty(JaggeryCoreConstants.JAGGERY_INCLUDES_CALLSTACK, new Stack<String>());
    }

    /*
    public final ScriptObject startEngine(Context context)throws IOException {
        if(context == null) {

        }
        final ScriptObject global = context.createGlobal();
        return global;
    }
    */


    private void initLibs(ScriptObject global) throws IllegalAccessException, InstantiationException {
        global.addOwnProperty("core", Property.NOT_ENUMERABLE, "buddhi");
    }

    /**
    * convert inputStream to String
    *
    * @param is     inputStream to be converted
    * */
    private String isToString(InputStream is) throws IOException {
        String string = IOUtils.toString(is, "UTF-8");
        return string;
    }

    public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext)NashornEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
    }

    public static void setJaggeryContext(JaggeryContext jaggeryContext) {
        NashornEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, jaggeryContext);
    }

    /**
    * read each core js modules in resource/lib folder, return them as InputStream
    *
    * @param    resource    string path to resource
    * */
    private InputStream resourcetoStream(final String resource) throws PrivilegedActionException {
        InputStream libScripts = AccessController.doPrivileged(
                new PrivilegedExceptionAction<InputStream>() {
                    @Override
                    public InputStream run() throws Exception {
                        return CommonManager.class.getResourceAsStream(resource);
                    }
                }
        );
        return libScripts;
    }

    Source source;

    public void corelibUpdate() throws PrivilegedActionException {
        Path dir = Paths.get("/home/buddhi/IdeaProjects/Jaggery2.0/modules"); //set dir correctly -----------
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for(Path file : stream) {
                System.out.println(file.getFileName());

                //add new file watcher here---------------------------------------------------------
                //cache this scripts sources.
                source = new Source(String.valueOf(file.getFileName()), file.toFile());
            }
        } catch (IOException | DirectoryIteratorException x) {
            System.out.println(x);
        }
    }

    public static Map<String, Boolean> getIncludes(JaggeryContext context) {
        return (Map<String, Boolean>)context.getProperty(JaggeryCoreConstants.JAGGERY_INCLUDED_SCRIPTS);
    }

    public static Stack<String> getCallStack(JaggeryContext context) {
        return (Stack<String>)context.getProperty(JaggeryCoreConstants.JAGGERY_INCLUDES_CALLSTACK);
    }

    private static void checkConfigPermission() {
        final SecurityManager sm = System.getSecurityManager();
        if(sm != null) {
            sm.checkPermission(new RuntimePermission(Context.NASHORN_SET_CONFIG));
        }
    }

    private static ClassLoader getAppClassLoader() {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        return (ccl == null) ? CommonManager.class.getClassLoader() : ccl;
    }
}

