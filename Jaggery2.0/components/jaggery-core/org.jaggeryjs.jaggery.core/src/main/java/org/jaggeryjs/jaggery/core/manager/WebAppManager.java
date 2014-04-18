package org.jaggeryjs.jaggery.core.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.engine.NashornEngine;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class WebAppManager {

    private static final Log log = LogFactory.getLog(WebAppManager.class);

    public static final String CORE_MODULE_NAME = "core";

    public static final String SERVLET_RESPONSE = "webappmanager.servlet.response";

    public static final String SERVLET_REQUEST = "webappmanager.servlet.request";

    private static final String DEFAULT_CONTENT_TYPE = "text/html";

    private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    //public static final String JAGGERY_MODULES_DIR = "modules";

    public static final String WS_REQUEST_PATH = "requestURI";

    public static final String WS_SERVLET_CONTEXT = "/websocket";

    private static final String SHARED_JAGGERY_CONTEXT = "shared.jaggery.context";

    private static final String SERVE_FUNCTION_JAGGERY = "org.jaggeryjs.serveFunction";

    private static final Map<String, List<String>> timeouts = new HashMap<String, List<String>>();

    private static final Map<String, List<String>> intervals = new HashMap<String, List<String>>();

    private static boolean isWebSocket = false;

    static {}

    public static void execute(HttpServletRequest request, HttpServletResponse response) throws
            IOException, ServletException {
        System.out.print("*********************************************************************************");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        String scriptPath = /*getScriptPath(request)*/"/home/buddhi/Documents/buddhi.js" ;

        outputStream = response.getOutputStream();
        inputStream = request.getServletContext().getResourceAsStream(scriptPath);

        /*if(inputStream == null) {
            System.out.print("6666666666666666666666666666666666666666666666666666666666666666666666666");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
            return;
        }       */

        //access this engine class throug common manager using singlton pattern
        NashornEngine engine = new NashornEngine();
        engine.run(System.in, outputStream, outputStream);
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

    /*public static NashornEngine getEngine() throws ScriptException {}

    public static void print() throws ScriptException{}

    public static ScriptObject executeScript() throws ScriptException{}

    public static String getNormalizedScriptPath() {}

    public static JaggeryContext sharedJaggeryContext() {}

    public static JaggeryContext clonedJaggeryContext() {}

    public static void deploy() throws ScriptException() {}

    public static void undeploy() {}



    public static String getScriptPath() {}

    private static String resolveScriptPath() {}

    private static String getPath() {}

    private static void definePrperties() {}

    private static JaggeryContext createJaggeryContext() {}

    protected static ScriptCachingContext getScriptCachingContext() throws ScriptException {}

    public static String[] getKeys() {}

    public static long getScriptLastModified() throws ScriptException{}

    private static String canonicalURI() {}

    private static boolean isPathSeparator() {}

    public static boolean isIsWebSocket() {}   */
}