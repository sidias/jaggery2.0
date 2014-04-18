package org.jaggeryjs.jaggery.core.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.engine.NashornEngine;
import org.jaggeryjs.scriptengine.exception.ScriptException;

//import org.wso2.carbon.context.CarbonContext;

/**
 * Shares the common functionality, of initialization of functions, and Host Objects
 */
public class CommonManager {

    private static final int BYTE_BUFFER_SIZE = 1024;

    private static final Log log = LogFactory.getLog(CommonManager.class);

    public static final String JAGGERY_URLS_MAP = "jaggery.urls.map";
    public static final String JAGGERY_OUTPUT_STREAM = "jaggery.output.stream";

    public static final String HOST_OBJECT_NAME = "RhinoTopLevel";

    private static CommonManager manager;

    private NashornEngine engine = null;
    //private ModuleManager moduleManager = null;

    private CommonManager() throws ScriptException {
    }

    // singleton pattern
    public static CommonManager getInstance() throws ScriptException {
        if (manager == null) {
            manager = new CommonManager();
        }
        return manager;
    }

    public NashornEngine getEngine() {
        return this.engine;
    }

    /*public ModuleManager getModuleManager() {
        return this.moduleManager;
    }  */

    /*public void initialize(String modulesDir, RhinoSecurityController securityController)
            throws ScriptException {
        this.engine = new NashornEngine(new CacheManager(null), new RhinoContextFactory(securityController));
        this.moduleManager = new ModuleManager(modulesDir);
        exposeDefaultModules(this.engine, this.moduleManager.getModules());
    }

    public static void initContext(JaggeryContext context) throws ScriptException {
        context.setEngine(manager.engine);
        context.setScope(manager.engine.getRuntimeScope());
        context.setTenantId(Integer.toString(CarbonContext.getCurrentContext().getTenantId()));

        context.addProperty(Constants.JAGGERY_CORE_MANAGER, manager);
        context.addProperty(Constants.JAGGERY_INCLUDED_SCRIPTS, new HashMap<String, Boolean>());
        context.addProperty(Constants.JAGGERY_INCLUDES_CALLSTACK, new Stack<String>());
    }






    public static boolean isHTTP(String url) {
        return url.matches("^[hH][tT][tT][pP][sS]?.*");
    }



    private static void exposeModule(Context cx, ScriptableObject object, JavaScriptModule module)
            throws ScriptException {
        for (JavaScriptHostObject hostObject : module.getHostObjects()) {
            RhinoEngine.defineHostObject(object, hostObject);
        }

        for (JavaScriptMethod method : module.getMethods()) {
            RhinoEngine.defineMethod(object, method);
        }

        for (JavaScriptScript script : module.getScripts()) {
            script.getScript().exec(cx, object);
        }
    }

    private static void exposeModule(RhinoEngine engine, JavaScriptModule module) {
        for (JavaScriptHostObject hostObject : module.getHostObjects()) {
            engine.defineHostObject(hostObject);
        }

        for (JavaScriptMethod method : module.getMethods()) {
            engine.defineMethod(method);
        }

        for (JavaScriptScript script : module.getScripts()) {
            engine.defineScript(script);
        }
    }

    /**
     * JaggeryMethod responsible of writing to the output stream
     */
    /*public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws ScriptException {
        String functionName = "print";
        JaggeryContext jaggeryContext = getJaggeryContext();

        int argsCount = args.length;
        if (argsCount != 1) {
            HostObjectUtil.invalidNumberOfArgs("RhinoTopLevel", functionName, argsCount, false);
        }
        OutputStream out = (OutputStream) jaggeryContext.getProperty(CommonManager.JAGGERY_OUTPUT_STREAM);
        if (args[0] instanceof StreamHostObject) {
            InputStream in = ((StreamHostObject) args[0]).getStream();
            try {
                byte[] buffer = new byte[BYTE_BUFFER_SIZE];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                in.close();
            } catch (IOException e) {
                log.debug(e.getMessage(), e);
                throw new ScriptException(e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.debug(e.getMessage(), e);
                    }
                }
            }
        } else {
            try {
                out.write(HostObjectUtil.serializeObject(args[0]).getBytes());
            } catch (IOException e) {
                log.debug(e.getMessage(), e);
                throw new ScriptException(e);
            }
        }
    }

    public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext) NashornEngine.getContextProperty(EngineConstants.JAGGERY_CONTEXT);
    }

    public static void setJaggeryContext(JaggeryContext jaggeryContext) {
        NashornEngine.putContextProperty(EngineConstants.JAGGERY_CONTEXT, jaggeryContext);
    }

    public static Map<String, Boolean> getIncludes(JaggeryContext jaggeryContext) {
        return (Map<String, Boolean>) jaggeryContext.getProperty(Constants.JAGGERY_INCLUDED_SCRIPTS);
    }

    public static Stack<String> getCallstack(JaggeryContext jaggeryContext) {
        return (Stack<String>) jaggeryContext.getProperty(Constants.JAGGERY_INCLUDES_CALLSTACK);
    }*/
}
