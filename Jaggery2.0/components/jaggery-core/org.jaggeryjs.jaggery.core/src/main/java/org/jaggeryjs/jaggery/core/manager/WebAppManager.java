package org.jaggeryjs.jaggery.core.manager;

import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
        }
    }

    public static void execute(HttpServletRequest request, HttpServletResponse response) throws
            IOException, ServletException {
        System.out.print("*********************************************************************************");

        InputStream inputStream = null;
        OutputStream outputStream = null;
        NashornEngine engine;
        //String scriptPath = getScriptPath(request) ;
        Context cx = null;
        ScriptFunction function = null;

        try {
            outputStream = response.getOutputStream();
            function = (ScriptFunction)request.getServletContext().getAttribute(
                    SERVE_FUNCTION_JAGGERY
            );
            String scriptPath = getScriptPath(request);
            inputStream = request.getServletContext().getResourceAsStream(scriptPath);
            engine = CommonManager.getInstance().getEngine();
            cx = engine.makeContext(System.in, outputStream, outputStream);
            ScriptObject global = CommonManager.getInstance().startEngine(cx);

            //change here
            //scriptPath ="/home/buddhi/Documents/buddhi.js";
            scriptPath = "/home/buddhi/Desktop/jaggery-0.9.0-SNAPSHOT/apps/coffeeshop/index.jag";
            CommonManager.getInstance().runScripts(cx, global, scriptPath);

        } catch (ScriptException e) {
            String msg = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    msg);
        } catch (PrivilegedActionException e) {
            e.printStackTrace();
        }
    }

    private static String getNormalizedScriptPath(String[] keys) {
        return "/".equals(keys[1]) ? keys[2] : keys[1] + keys[2];
    }

    public static void deploy(org.apache.catalina.Context context) throws ScriptException {
        ServletContext ctx = context.getServletContext();
        Context cx = Context.getContext();


    }

    public static void undeploy(org.apache.catalina.Context context) {}

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
    /*
    protected static ScriptCachingContext getScriptCachingContext(HttpServletRequest request, String scriptPath)
            throws ScriptException {

    }  */

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
}
