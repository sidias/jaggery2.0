package org.jaggeryjs.scriptengine.engine;

import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.internal.runtime.*;
import jdk.nashorn.internal.runtime.options.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class NashornEngine {

    public static final int SUCCESS = 0;
    public static final int COMPILATION_ERROR = 101;
    public static final int RUNTIME_ERROR = 102;


    private static final Log log = LogFactory.getLog(NashornEngine.class);
    //do not turn this into to static.access this throug singleton pattern as in commaon manager
    //param @args is igonored
    public final int run(final InputStream in, final OutputStream out, final OutputStream err)throws IOException{

        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        final Context context = makeContext(in, out, err);
        if(context == null) {
            System.out.println("(((((((((((((((((context null)))))))))))))))))))))))");
        }

        final ScriptObject global = context.createGlobal();
        final ScriptEnvironment env = context.getEnv();
        final List<String> files = env.getFiles();//how this env get files

        /*if(files.isEmpty()) {
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            return 0; //return servlet exception
        }   */
        List<String> file = new LinkedList<>();
        file.add("/home/buddhi/Documents/buddhi.js");

        return runScripts(context, global, file);
    }

    //param @args ignored
    private static Context makeContext(final InputStream in, final OutputStream out, final OutputStream err) {
        final PrintStream pout = out instanceof PrintStream ? (PrintStream) out : new PrintStream(out);
        final PrintStream perr = err instanceof PrintStream ? (PrintStream) err : new PrintStream(err);
        final PrintWriter wout = new PrintWriter(pout, true);
        final PrintWriter werr = new PrintWriter(perr, true);

        final ErrorManager errors = new ErrorManager(werr);
        final Options options = new Options("nashorn", werr);

         return new Context(options, errors, wout, werr, Thread.currentThread().getContextClassLoader());
    }

    private int runScripts(final Context context, final ScriptObject global, final List<String> files) throws IOException {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++" + files.get(0).toString());
        final ScriptObject oldGlobal = Context.getGlobal();
        final boolean globalChanged = (oldGlobal != global);
        try{
            if(globalChanged) {
                Context.setGlobal(global);
            }
            final ErrorManager errors = context.getErrorManager();

            //for now just get the file and read it but later this file will be a string return from parser
            //to get file to parser use Source.java class methods.
            for(final String fileName : files) {
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
            }

        } finally {
            // == contextFactory getout in rahino
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





























    /*private CacheManager cacheManager;

    static {}

    public NashornEngine(CacheManager cacheManager,) {}

    public static void defineProperty(ScriptObject scope, JavaScriptProperty property) {

    }

    public void defineProperty(JavaScriptProperty property) {

    }

    public static void defineScript(ScriptObject scope, JavaScriptScript script) {
        Context cx = enterGlobalContext();
        script.getScript().exec(cx,scope);
        extiContext();    }

    public void defineScript() {}

    public static void defineMethod() {}

    public void defineMethod() {}

    public Object eval() {}

    public Object eval() {}

    public ScriptObject exec() {}

    public void exec() {}

    public Object call() {}

    public Object call() {}

    public Object call() {}

    public ScriptObject getRuntimeScope() {}

    public void unloadTenant() {}

    public static void putContextProperty() {}

    public static Object getContextProperty() {}

    public Context enterContext() {}

    public static Context enterContext() {}

    public static Context enterGlobalContext() {}

    public static void extiContext() {}

    private void defineClass() {}

    private void defineMethod() {}

    private CacheManager getCacheManager() {}

    private Object execFunc() {}

    private static Object execFunc() {}

    private Object evalScript() {}

    private ScriptObject execScript() {}

    private static ScriptObject removeUnsafeObjects() {}

    private static void copyEngineScope() {}   */
}
