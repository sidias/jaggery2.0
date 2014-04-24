package org.jaggeryjs.jaggery.core.manager;

import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.internal.runtime.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.parser.JaggeryParser;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.NashornEngine;
import org.jaggeryjs.scriptengine.exception.ScriptException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class CommonManager {

    private static final Log log = LogFactory.getLog(CommonManager.class);

    public static final String JAGGERY_URLS_MAP = "jaggery.urls.map";
    public static final String JAGGERY_CONTEXT = "engine.jaggeryContext";

    private final String JAGGERY_INIT_SCRIPT = "/jaggery.js";

    private static final int SUCCESS = 0;
    private static final int COMPILATION_ERROR = 101;
    private static final int RUNTIME_ERROR = 102;

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

    public void initialize(String moduleDir) throws ScriptException{
        this.engine = new NashornEngine(new CacheManager(null));
        this.moduleDir = moduleDir;
    }

    public final ScriptObject startEngine(Context context)throws IOException {

        if(context == null) {

        }
        final ScriptObject global = context.createGlobal();
        //final ScriptEnvironment env = context.getEnv();
        //final List<String> files = env.getFiles();//how this env get files
        return global;
    }

    public int runScripts(final Context context, final ScriptObject global, final String fileName) throws IOException, PrivilegedActionException, ScriptException {

        final ScriptObject oldGlobal = Context.getGlobal();
        final boolean globalChanged = (oldGlobal != global);
        try{
            if(globalChanged) {
                Context.setGlobal(global);
            }
            final ErrorManager errors = context.getErrorManager();

            final URL init_script = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<URL>() {
                        @Override
                        public URL run() throws Exception {
                            return CommonManager.class.getResource(JAGGERY_INIT_SCRIPT);
                        }
                    });

            context.load(global,init_script);
            InputStream stream = new FileInputStream(fileName);

            InputStream s = JaggeryParser.parse(stream);
            //for now just get the file and read it but later this file will be a string return from parser
            //to get file to parser use Source.java class methods.

            //final File file = new File(fileName);
            final ScriptFunction script = context.compileScript(new Source(fileName,/*file.toURI().toURL()*/isToString(s)), global);
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

    public static JaggeryContext getJaggeryContext() {
        return (JaggeryContext)NashornEngine.getContextProperty(JAGGERY_CONTEXT);
    }

    /*
    * convert inputStream to String
    *
    * @param is     inputStream to be converted
    * */
    private String isToString(InputStream is) throws IOException {
        String string = IOUtils.toString(is, "UTF-8");
        return string;
    }
}
