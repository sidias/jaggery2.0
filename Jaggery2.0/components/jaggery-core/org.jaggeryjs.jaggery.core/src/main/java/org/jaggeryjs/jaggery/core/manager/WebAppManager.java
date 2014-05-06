package org.jaggeryjs.jaggery.core.manager;

import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.jaggeryjs.jaggery.core.ScriptReader;
import org.jaggeryjs.scriptengine.cache.ScriptCachingContext;
import org.jaggeryjs.scriptengine.engine.JaggeryContext;
import org.jaggeryjs.scriptengine.engine.JavascriptProperty;
import org.jaggeryjs.scriptengine.engine.NashornEngine;
import org.jaggeryjs.scriptengine.exception.ScriptException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.PrivilegedActionException;
import java.util.*;

public class WebAppManager {

    private static final Log log = LogFactory.getLog(WebAppManager.class);

    public static final String CORE_MODULE_NAME = "core";

    public static final String SERVLET_RESPONSE = "webappmanager.servlet.response";

    public static final String SERVLET_REQUEST = "webappmanager.servlet.request";

    private static final String DEFAULT_CONTENT_TYPE = "text/html";

    private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    public static final String JAGGERY_MODULES_DIR = "modules";

    public static final String WS_REQUEST_PATH = "requestURI";

    public static final String WS_SERVLET_CONTEXT = "/websocket";

    private static final String SHARED_JAGGERY_CONTEXT = "shared.jaggery.context";

    private static final String SERVE_FUNCTION_JAGGERY = "org.jaggeryjs.serveFunction";

    private static final Map<String, List<String>> timeouts = new HashMap<String, List<String>>();

    private static final Map<String, List<String>> intervals = new HashMap<String, List<String>>();

    private static boolean isWebSocket = false;

    //edit this block
    static {
        try {
            //String jaggeryDir = System.getProperty("jaggery.home");
            //if(jaggeryDir == null ) {
               // throw new ScriptException("Unable to find jaggery.home system properties");
            //}

            //String moduleDir = jaggeryDir + File.separator + JAGGERY_MODULES_DIR;
            String moduleDir = "";
            CommonManager.getInstance().initialize(moduleDir);
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PrivilegedActionException e) {
            e.printStackTrace();
        }

    }

    public static void execute(HttpServletRequest request, HttpServletResponse response) throws
            IOException, ServletException, ScriptException {


        System.out.print("********************************--------");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        NashornEngine engine = null;

        Context context = null;
        JaggeryContext jaggeryContext = null;
        ScriptFunction function = null;

        try {
            outputStream = response.getOutputStream();
            function = (ScriptFunction)request.getServletContext().getAttribute(
                    SERVE_FUNCTION_JAGGERY
            );
            String scriptPath = getScriptPath(request);
            inputStream = request.getServletContext().getResourceAsStream(scriptPath);
            if(inputStream == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
            }

            engine = CommonManager.getInstance().getEngine();
            context = engine.enterContext();

            scriptPath ="/home/buddhi/Documents/buddhi.js";
            //scriptPath = "/home/buddhi/Desktop/jaggery-0.9.0-SNAPSHOT/apps/coffeeshop/index.jag";

            jaggeryContext = createJaggeryContext(context, outputStream, scriptPath, request, response);
            //jaggeryContext.addProperty();

            if(function != null) {
                HttpServletRequest servletRequest = (HttpServletRequest) jaggeryContext.getProperty(SERVLET_REQUEST);
                //do other stuff --------------------
            }  else {
                inputStream = request.getServletContext().getResourceAsStream(scriptPath);
                if(inputStream == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
                    return;
                }

                CommonManager.getInstance().getEngine().
                        exec(new ScriptReader(inputStream), jaggeryContext.getScope(),
                                getScriptCachingContext(request, scriptPath));
            }


            //habai me stream tika set karthdinam aulak enawa.kohomada browser 1 stream
            //1ta me tika denne.

            //cx = engine.makeContext(System.in, outputStream, outputStream);
            //ScriptObject global = CommonManager.getInstance().startEngine(context);

            //CommonManager.getInstance().runScripts(cx, global, scriptPath);

        } catch (ScriptException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    msg);
        } finally {
            if(engine != null) {
                context.getOut().flush();
                context.getErr().flush();
            }
        }
    }

    private static String getNormalizedScriptPath(String[] keys) {
        return "/".equals(keys[1]) ? keys[2] : keys[1] + keys[2];
    }

    public static JaggeryContext sharedJaggeryContext(ServletContext sctx) throws ScriptException {

        JaggeryContext sharedContext = new JaggeryContext();
        CommonManager.initContext(sharedContext);

        sharedContext.addProperty(JaggeryCoreConstants.SERVLET_CONTEXT, sctx);

        ScriptObject sharedScope = sharedContext.getScope();
        JavascriptProperty application = new JavascriptProperty("application");
        application.setValue(new Object[]{sctx});
        NashornEngine.defineProperty(sharedScope, application);

        sctx.setAttribute(SHARED_JAGGERY_CONTEXT, sharedContext);

        //--------------------------------------------------------
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " +(JaggeryContext) sctx.getAttribute(SHARED_JAGGERY_CONTEXT) );
        return (JaggeryContext) sctx.getAttribute(SHARED_JAGGERY_CONTEXT);
    }

    public static JaggeryContext clonedJaggeryContext(ServletContext sctx) throws ScriptException {
        JaggeryContext shared = sharedJaggeryContext(sctx);
        NashornEngine engine = shared.getEngine();
        Context cx = Context.getContext();

        ScriptObject instanceScope = cx.createGlobal();

        JaggeryContext clone = new JaggeryContext();
        clone.setEngine(engine);
        clone.setTenantId(shared.getTenantId());
        clone.setScope(instanceScope);

        clone.addProperty(JaggeryCoreConstants.SERVLET_CONTEXT,
                shared.getProperty(JaggeryCoreConstants.SERVLET_CONTEXT));
        clone.addProperty(JaggeryCoreConstants.JAGGERY_CORE_MANAGER,
                shared.getProperty(JaggeryCoreConstants.JAGGERY_CORE_MANAGER));
        clone.addProperty(JaggeryCoreConstants.JAGGERY_INCLUDED_SCRIPTS, new HashMap<String, Boolean>());
        clone.addProperty(JaggeryCoreConstants.JAGGERY_INCLUDES_CALLSTACK, new Stack<String>());

        return clone;
    }

    public static void deploy(org.apache.catalina.Context context) throws ScriptException {
        ServletContext ctx = context.getServletContext();

        JaggeryContext sharedContext = new JaggeryContext();
        CommonManager.initContext(sharedContext);

        sharedContext.addProperty(JaggeryCoreConstants.SERVLET_CONTEXT, ctx);

        ScriptObject sharedScope = sharedContext.getScope();
        JavascriptProperty application = new JavascriptProperty("application");
        application.setValue(new Object[]{ctx});
        NashornEngine.defineProperty(sharedScope, application);

        ctx.setAttribute(SHARED_JAGGERY_CONTEXT, sharedContext);
    }

    public static void undeploy(org.apache.catalina.Context context) {
        String contextPath = context.getServletContext().getContextPath();
        List<String> taskIds = timeouts.get(contextPath);
        if(taskIds != null) {
            for(String taskId : taskIds) {

            }
        }
        taskIds = intervals.get(contextPath);
        if(taskIds != null) {

        }
        log.debug("Releasing resources of : " + context.getServletContext().getContextPath());
    }

    private static String getScriptPath(HttpServletRequest request) {
        String url = request.getServletPath();
        Map<String, Object> urlMappings = (Map<String, Object>)request.getServletContext().getAttribute(CommonManager.JAGGERY_URLS_MAP);
        if(urlMappings == null) {
            return url;
        }
        String path;
        if(url.equals("/")) {
            path = getPath(urlMappings, url);
        } else {
            path = resolveScriptPath(new ArrayList<String>(Arrays.asList(url.substring(1).split("/", -1))), urlMappings);
        }

        return path == null ? url : path;
    }

    private static String getPath(Map<String, Object> map, String part) {
        Object obj = "/".equals(part) ? null : map.get(part);
        if(obj == null) {
            obj = map.get("/");
            if(obj != null) {
                return (String)obj;
            }
            obj = map.get("*");
            String path = (String)obj;
            if(path != null && path.endsWith("*")) {
                return path.substring(0, path.length() - 1) + part;
            } else {
                return path;
            }
        }
        if(obj instanceof String) {
            return (String)obj;
        }
        map = (Map<String, Object>)obj;
        obj = map.get("/");
        if(obj != null) {
            return (String)obj;
        }
        obj = map.get("*");
        return (String)obj;
    }

    public static boolean isWebSocket(ServletRequest request) {
        isWebSocket = "websocket".equals(((HttpServletRequest)request).getHeader("Upgrade"));
        return "websocket".equals(((HttpServletRequest) request).getHeader("Upgrade"));
    }

    private static String resolveScriptPath(List<String> parts, Map<String, Object> map) {
        String part = parts.remove(0);
        if(parts.isEmpty()) {
            return getPath(map, part);
        }
        Object obj = map.get(part);
        if(obj == null) {
            return getPath(map, "/");
        }
        if(obj instanceof Map) {
            return resolveScriptPath(parts, (Map<String, Object>) obj);
        }
        return null;
    }

    public static String[] getKeys(String context, String parent, String scriptPath) {
        String path;
        String normalizedScriptPath;
        context = context.equals("") ? "/" : context;
        normalizedScriptPath = scriptPath.startsWith("/") ?
                FilenameUtils.normalize(scriptPath, true) :
                FilenameUtils.normalize(FilenameUtils.getFullPath(parent) + scriptPath, true);
        path = FilenameUtils.getFullPath(normalizedScriptPath);
        path = path.equals("/") ? path : path.substring(0, path.length() - 1);
        normalizedScriptPath = "/" + FilenameUtils.getName(normalizedScriptPath);
        return new String[] {
                context,
                path,
                normalizedScriptPath
        };
    }

    private static JaggeryContext createJaggeryContext(Context ctx, OutputStream out, String scriptPath,
                                                       HttpServletRequest request, HttpServletResponse response) throws ScriptException {
        ServletContext servletContext = request.getServletContext();
        JaggeryContext context = clonedJaggeryContext(servletContext);
        CommonManager.setJaggeryContext(context);
        context.addProperty(SERVLET_REQUEST, request);
        context.addProperty(SERVLET_RESPONSE, response);

        CommonManager.getCallStack(context).push(scriptPath);
        CommonManager.getIncludes(context).put(scriptPath, true);

        context.addProperty(CommonManager.JAGGERY_OUTPUT_STREAM, out);
        defineProperties(ctx, context, context.getScope());
        return context;
    }

    //wrap value as scriptObject.
    //may throw exception. check.
    private static void defineProperties(Context context, JaggeryContext jaggeryContext, ScriptObject scope) {
        JavascriptProperty request = new JavascriptProperty("request");
        HttpServletRequest servletRequest = (HttpServletRequest)jaggeryContext.getProperty(SERVLET_REQUEST);
        request.setValue(new Object[]{servletRequest});
        NashornEngine.defineProperty(scope, request);

        JavascriptProperty response = new JavascriptProperty("response");
        HttpServletResponse servletResponse = (HttpServletResponse)jaggeryContext.getProperty(SERVLET_RESPONSE);
        response.setValue(new Object[]{servletResponse});
        NashornEngine.defineProperty(scope, response);

        JavascriptProperty session = new JavascriptProperty("session");
        session.setValue(new Object[]{servletRequest.getSession()});
        NashornEngine.defineProperty(scope, session);

        JavascriptProperty application = new JavascriptProperty("application");
        ServletContext servletContext = (ServletContext)jaggeryContext.getProperty(JaggeryCoreConstants.SERVLET_CONTEXT);
        application.setValue(new Object[]{servletContext});
        NashornEngine.defineProperty(scope, application);

        if(isWebSocket(servletRequest)) {
            JavascriptProperty webSocket = new JavascriptProperty("webSocket");
            webSocket.setValue(new Object[0]);
            NashornEngine.defineProperty(scope, webSocket);
        }
    }

    protected static ScriptCachingContext getScriptCachingContext(HttpServletRequest request, String scriptPath)
            throws ScriptException{
        JaggeryContext jaggeryContext = CommonManager.getJaggeryContext();
        String tenantId = jaggeryContext.getTenantId();
        String[] parts = getKeys(request.getContextPath(), scriptPath, scriptPath);
        ScriptCachingContext sctx = new ScriptCachingContext(tenantId, parts[0], parts[1], parts[2]);

        ServletContext servletContext = request.getServletContext();
        long lastModified = getScriptLastModified(servletContext, scriptPath);
        sctx.setSourceModifiedTime(lastModified);
        return sctx;
    }

    public static long getScriptLastModified(ServletContext servletContext, String scriptPath)
            throws ScriptException{
        long result = -1;
        URLConnection uc = null;
        try {
            URL scriptUrl = servletContext.getResource(canonicalURI(scriptPath));
            if(scriptUrl == null) {
                String msg = "Requested resource " + scriptPath + "cannot be found";
                log.error(msg);
                throw new ScriptException(msg);
            }
            uc = scriptUrl.openConnection();
            if(uc instanceof JarURLConnection) {
                result = ((JarURLConnection) uc).getJarEntry().getTime();
            } else {
                result = uc.getLastModified();
            }
        } catch (IOException e) {
            log.warn("Error getting last modified time for " + scriptPath, e);
            result = -1;
        } finally {
            if(uc != null) {
                try {
                    uc.getInputStream().close();
                } catch (IOException e) {
                    log.error("Erro closing input stream for script " + scriptPath, e);
                }
            }
        }
        return result;
    }

    private static String canonicalURI(String s) {
        if(s == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        final int len = s.length();
        int pos = 0;
        while (pos < len) {
            char c = s.charAt(pos);
            if(isPathSeparator(c)) {
                while (pos + 1 < len && isPathSeparator(s.charAt(pos + 1))) {
                    ++pos;
                }

                if(pos +1 < len && s.charAt(pos +1) == '.') {
                    if(pos +2 >= len) {
                        break;
                    }

                    switch (s.charAt(pos +2)) {
                        case '/':
                        case '\\':
                            pos += 2;
                            continue;

                        case '.':
                            if(pos +3 <len && isPathSeparator(s.charAt(pos +3))) {
                                pos += 3;
                                int separatorPos = result.length() -1;
                                while (separatorPos >= 0 && !isPathSeparator(result.charAt(separatorPos))) {
                                    --separatorPos;
                                }
                                if(separatorPos >= 0) {
                                    result.setLength(separatorPos);
                                }
                                continue;
                            }
                    }
                }
            }
            result.append(c);
            ++pos;
        }
        return result.toString();
    }

    private static boolean isPathSeparator(char c) {
        return (c == '/' || c == '\\');
    }
}
