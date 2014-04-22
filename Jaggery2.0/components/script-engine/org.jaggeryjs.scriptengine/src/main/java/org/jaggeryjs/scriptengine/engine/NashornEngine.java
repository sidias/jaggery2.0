package org.jaggeryjs.scriptengine.engine;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.runtime.*;
import jdk.nashorn.internal.runtime.options.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.cache.CacheManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static jdk.nashorn.internal.lookup.Lookup.MH;

public class NashornEngine {

    private CacheManager cacheManager;
    private static boolean debugMode = false;

    private static NashornScriptEngine engine = null;
    private static NashornScriptEngineFactory engineFactory = null;


    private static final Log log = LogFactory.getLog(NashornEngine.class);

    static {
        engineFactory = new NashornScriptEngineFactory();
        engine = (NashornScriptEngine)engineFactory.getScriptEngine();
    }

    public NashornEngine(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public NashornEngine() {}

    private CacheManager getCacheManager() {
        return cacheManager;
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


    public Context makeContext(final InputStream in, final OutputStream out, final OutputStream err) {
        final PrintStream pout = out instanceof PrintStream ? (PrintStream) out : new PrintStream(out);
        final PrintStream perr = err instanceof PrintStream ? (PrintStream) err : new PrintStream(err);
        final PrintWriter wout = new PrintWriter(pout, true);
        final PrintWriter werr = new PrintWriter(perr, true);

        final ErrorManager errors = new ErrorManager(werr);
        final Options options = new Options("nashorn", werr);

        return new Context(options, errors, wout, werr, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Exists from the context associated in the current thread
     */
    public static void exitContext(Context context) {

    }

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

    /**
     * Unloads all the resources associated with a particular tenant
     *
     * @param tenantId Tenant to be unloaded
     */
    /*public void unloadTenant(String tenantId) {
        this.cacheManager.unloadTenant(tenantId);
    }*/


}
