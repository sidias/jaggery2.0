package org.jaggeryjs.scriptengine.engine;

import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.*;
import jdk.nashorn.internal.runtime.options.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.exception.ScriptException;

import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.security.*;

import static jdk.nashorn.internal.lookup.Lookup.MH;

public class NashornEngine {

    private static final Log log = LogFactory.getLog(NashornEngine.class);

    private CacheManager cacheManager;
    private static boolean debugMode = false;
    private static String debugPort = "-1";

    private static final int SUCCESS = 0;
    private static final int COMPILATION_ERROR = 101;
    private static final int RUNTIME_ERROR = 102;

    private final Context context;
    private final ScriptObject global;

    private static final AccessControlContext CREATE_CONTEXT_ACC_CTXT = createPermAccCtxt(Context.NASHORN_CREATE_CONTEXT);
    private static final AccessControlContext CREATE_GLOBAL_ACC_CTXT  = createPermAccCtxt(Context.NASHORN_CREATE_GLOBAL);

    public NashornEngine(CacheManager cacheManager, final ClassLoader appLoader) throws PrivilegedActionException, IOException, ScriptException {
        this.cacheManager = cacheManager;
        final Options options = new Options("nashorn");

        //throw ParseException on first error from script
        final ErrorManager errorManager = new Context.ThrowErrorManager();

        //create new Nashron Context
        this.context = AccessController.doPrivileged(new PrivilegedAction<Context>() {
            @Override
            public Context run() {
                try {
                    return new Context(options, errorManager, appLoader);
                } catch (final RuntimeException e) {
                    if(Context.DEBUG) {
                        e.printStackTrace();
                    }
                    throw e;
                }
            }
        }, CREATE_CONTEXT_ACC_CTXT);

        this.global = createNashornGlobal();
        this.context.setGlobal(global);

        //evaluate jaggery initial script
        try {
            loadJaggeryScript();
        } catch (final ScriptException e) {
            if(Context.DEBUG) {
                e.printStackTrace();
            }
            throw new ScriptException("initial script (jaggery.js) load");
        }
    }

    private void loadJaggeryScript() throws ScriptException {
        final String script = "/jaggery.js";
        try {
            final URL url = AccessController.doPrivileged(new PrivilegedExceptionAction<URL>() {
                @Override
                public URL run() throws Exception {
                    return NashornEngine.class.getResource(script);
                }
            });
            //load jaggery.js script to this context.
            this.context.load(this.global, url);

        } catch (PrivilegedActionException e) {
            if(Context.DEBUG) {
                e.printStackTrace();
            }
            throw new ScriptException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //there is a method to create nashorn global object.if need we can add
    //that method also.

    private static AccessControlContext createPermAccCtxt(final String permName) {
        final Permissions perms = new Permissions();
        perms.add(new RuntimePermission(permName));
        return new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, perms) });
    }

    private ScriptObject createNashornGlobal() {
        final ScriptObject newGlobal = AccessController.doPrivileged(new PrivilegedAction<ScriptObject>() {
            @Override
            public ScriptObject run() {
                try {
                    return context.createGlobal();
                } catch (final RuntimeException e) {
                    if(Context.DEBUG) {
                        e.printStackTrace();
                    }
                    throw e;
                }
            }
        }, CREATE_GLOBAL_ACC_CTXT);

        context.initGlobal(newGlobal);
        return newGlobal;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Context enterContext() {
        return this.context;
    }

    //check this is correct or wrong
    public static void defineMethod(ScriptObject scope, JavascriptMethods methods) {
        String name = methods.getName();
        MethodHandle methodHandle = MH.findStatic(
                MethodHandles.lookup(),
                NashornEngine.class,
                name,
               MH.type(
                       Object.class,
                       ScriptFunction.class,
                       Object.class));
        ScriptFunction function = Global.instance().newScriptFunction(name, methodHandle,scope, false);
        scope.addOwnProperty(name, Property.NOT_ENUMERABLE, function);
    }

    public void exec(Reader scriptReader, ScriptObject scope, ScriptCachingContext scriptCachingContext)
            throws ScriptException {
        if(scope == null) {
            String msg = "ScriptObject value for scope, can not be null";
            log.error(msg);
            throw new ScriptException(msg);
        }
        execScripts(scriptReader, scope, scriptCachingContext);
    }

    private void execScripts(Reader scriptReader, ScriptObject scope,
                             ScriptCachingContext scriptCachingContext) throws ScriptException {
        Context cx = enterContext();
       System.out.println("script is executing -------------------------");
    }

    //we can replace this context with entercontext
    /*private int runScripts(final Context context, final ScriptObject global, final String fileName) throws IOException, PrivilegedActionException, ScriptException {

        final ScriptObject oldGlobal = Context.getGlobal();
        final boolean globalChanged = (oldGlobal != global);
        try{
            if(globalChanged) {
                Context.setGlobal(global);
            }
            final ErrorManager errors = context.getErrorManager();

            //jaggery.js file will load each time when context is created.(correct way);
            //jaggery js only read @ once with context creation,
            //initScripts(context, global, JAGGERY_INIT_SCRIPT);

            //initLibs(global);

            InputStream stream = new FileInputStream(fileName);
            InputStream s = JaggeryParser.parse(stream);

            final ScriptFunction script = context.compileScript(new Source(fileName,isToString(s)), global);
            if(script == null || errors.getNumberOfErrors() != 0) {
                return COMPILATION_ERROR;
            }

            try {
                apply(script, global);
            } catch (final NashornException e) {
                errors.error(e.toString());
                if (context.getEnv()._dump_on_error) {
                    e.printStackTrace(context.getErr());
                }

                return RUNTIME_ERROR;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            context.getOut().flush();
            context.getErr().flush();
            if(globalChanged) {
                context.setGlobal(oldGlobal);
            }
        }
        return SUCCESS;
    }   */

    /*private Context makeContext(final InputStream in, final OutputStream out, final OutputStream err) {
        final PrintStream pout = out instanceof PrintStream ? (PrintStream) out : new PrintStream(out);
        final PrintStream perr = err instanceof PrintStream ? (PrintStream) err : new PrintStream(err);
        final PrintWriter wout = new PrintWriter(pout, true);
        final PrintWriter werr = new PrintWriter(perr, true);

        final ErrorManager errors = new ErrorManager(werr);
        final Options options = new Options("nashorn", werr);

        return new Context(options, errors, wout, werr, Thread.currentThread().getContextClassLoader());
    } */

    /**
     * Gets a property from the Context of the current thread
     *
     * @param key Property name
     * @return Property value
     */
    public static Object getContextProperty(Object key) {
        ScriptObject obj = Context.getGlobal();
        return obj.getProperty(key.toString());
    }

    /**
     * Puts a property in the Context of the current thread
     *
     * @param key   Property name
     * @param value Property value
     */
    public static void putContextProperty(Object key, Object value) {
        Global currentGlobal = Global.instance();
        currentGlobal.addOwnProperty(key.toString(), Property.NOT_ENUMERABLE, value);
    }

    private Object apply(final ScriptFunction target, final Object self) {
        return ScriptRuntime.apply(target, self);
    }

    /**
     * Unloads all the resources associated with a particular tenant
     *
     */
    /*public void unloadTenant(String tenantId) {
        this.cacheManager.unloadTenant(tenantId);
    }*/

    /*
    * TODO: if need to register any module or object to the current
    * global scope in java level. follow the committed code
    *       JavascriptProperty property = new JavascriptProperty(name);
            property.setValue(value);
            property.setAttribute(attribute);
            defineProperty(property);
    *
    *       @param  name        name of the property
    *       @param  attribute   can be acquired through Property.NOT_ENUMERABLE
    *       @param  value       value of the property
    * */
    public ScriptObject getRuntimeScope() throws ScriptException {
        ScriptObject scope = removeUnsafeObjects(Context.getGlobal());
        return scope;
    }

    /**
     * This method registers the specified property in the specified scope.
     *
     * @param scope    The scope to register the bean object
     * @param property Property to be defined
     */
    public static void defineProperty(ScriptObject scope, JavascriptProperty property) {
        String name = property.getName();
        Object value = property.getValue();
        int attr = property.getAttribute();

        if((value instanceof Number) || (value instanceof String)
                || (value instanceof Boolean)) {
            scope.addOwnProperty(name, attr, value);
        } else {
            Object wrapped = Global.instance().wrapAsObject(value);
            scope.addOwnProperty(name, attr, wrapped);
        }
    }

    /**
     * This method registers the specified property in the current scope.
     *
     * @param property Property to be defined
     */
    public void defineProperty(JavascriptProperty property) {
        String name = property.getName();
        int attr = property.getAttribute();

        ScriptObject scope = Context.getGlobal();
        Object wrapped = Global.instance().wrapAsObject(property.getValue());
        scope.addOwnProperty(name, attr, wrapped);
    }

    /*
        * use this method implementation to remove builtin objects that
        * unneccesary to current jaggery run.
        *
        * TODO: remove unneeded object from global
        * */
     private static ScriptObject removeUnsafeObjects(ScriptObject global) {
         //global.remove();
         return global;
    }

    /**
    * load a scripFile from source url to specified scope
    *
    * @param    scope   the scope to load script(global scope).
    * @param    url     URL to resource.
    *
    * always return undefined.if script cannot be loaded javascript
    * type Error will be throwing.
    * */
    public Object loadScripts(Context context, ScriptObject scope, URL url) throws IOException {
        return context.load(scope, url);
    }
}
