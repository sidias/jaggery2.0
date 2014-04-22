package org.jaggeryjs.jaggery.core.manager;

import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.internal.runtime.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.NashornEngine;
import org.jaggeryjs.scriptengine.exception.ScriptException;

import java.io.File;
import java.io.IOException;

public class CommonManager {

    private static final Log log = LogFactory.getLog(CommonManager.class);

    public static final String JAGGERY_URLS_MAP = "jaggery.urls.map";
    public static final String JAGGERY_CONTEXT = "engine.jaggeryContext";

    private static final int SUCCESS = 0;
    private static final int COMPILATION_ERROR = 101;
    private static final int RUNTIME_ERROR = 102;

    private static CommonManager manager;
    private NashornEngine engine = null;

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

    public void initialize() throws ScriptException{
        this.engine = new NashornEngine(new CacheManager(null));
    }

    public final ScriptObject startEngine(Context context)throws IOException {

        if(context == null) {

        }
        final ScriptObject global = context.createGlobal();
        //final ScriptEnvironment env = context.getEnv();
        //final List<String> files = env.getFiles();//how this env get files

        return global;
    }

    //param @args ignored
    public int runScripts(final Context context, final ScriptObject global, final String fileName) throws IOException {

        final ScriptObject oldGlobal = Context.getGlobal();
        final boolean globalChanged = (oldGlobal != global);
        try{
            if(globalChanged) {
                Context.setGlobal(global);
            }
            final ErrorManager errors = context.getErrorManager();
            //loads jaggery core to current context.
            //context.load(global, );

            //for now just get the file and read it but later this file will be a string return from parser
            //to get file to parser use Source.java class methods.

            final File file = new File(fileName);
            final ScriptFunction script = context.compileScript(new Source(fileName, file.toURI().toURL()), global);
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
        } finally {
            context.getOut().flush();
            context.getErr().flush();
            if(globalChanged) {
                context.setGlobal(oldGlobal);
            }
        }
        return SUCCESS;
    }

    private Object apply(final ScriptFunction target, final Object self) {
        return ScriptRuntime.apply(target, self);
    }

    public static void setJaggeryContext(JaggeryContext jaggeryContext) {
        NashornEngine.putContextProperty(JAGGERY_CONTEXT, jaggeryContext);
    }

    /*
    * jaggery context can be use to load other properties to
    * current global scope
    * */
     public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext)NashornEngine.getContextProperty(JAGGERY_CONTEXT);
    }
}
