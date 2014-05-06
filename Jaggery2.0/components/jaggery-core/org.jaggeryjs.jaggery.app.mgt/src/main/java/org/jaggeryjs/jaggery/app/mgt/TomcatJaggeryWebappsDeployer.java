package org.jaggeryjs.jaggery.app.mgt;

import org.apache.axis2.util.IOUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.deploy.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.jaggery.core.JaggeryCoreConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import sun.nio.ch.IOUtil;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * This deployer is responsible for deploying/undeploying/updating jaggery apps*/
public class TomcatJaggeryWebappsDeployer {

    private Log log = LogFactory.getLog(TomcatJaggeryWebappsDeployer.class);

    private static class JaggeryConfListener implements LifecycleListener {

        private JSONObject jaggeryConfig;

        private JaggeryConfListener(JSONObject jaggeryConfig) {
            this.jaggeryConfig = jaggeryConfig;
        }

        @Override
        public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
            if(Lifecycle.BEFORE_START_EVENT.equals(lifecycleEvent.getType())) {

                return;
            }
        }
    }

    private static void initJaggeryappDefaults(Context context, JSONObject jaggeryConfig) {

        Tomcat.addServlet(context, JaggeryCoreConstants.JAGGERY_SERVLET_NAME, JaggeryCoreConstants.JAGGERY_SERVLET_CLASS);
        Tomcat.addServlet(context, JaggeryCoreConstants.JAGGERY_WEBSOCKET_SERVLET_NAME, JaggeryCoreConstants.JAGGERY_WEBSOCKET_SERVLET_CLASS);

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
        filterDef.setFilterClass(JaggeryCoreConstants.JAGGERY_FILTER_CLASS);
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(JaggeryCoreConstants.JAGGERY_FILTER_NAME);
        filterMap.addURLPattern(JaggeryCoreConstants.JAGGERY_URL_PATTERN);
        context.addFilterMap(filterMap);

        context.addApplicationListener(JaggeryCoreConstants.JAGGERY_APPLICATION_SESSION_LISTENER);
        addWelcomeFiles(context, jaggeryConfig);

        //jaggery conf param is null if conf file is not available.
        if(jaggeryConfig != null) {
            setDisplayName(context, jaggeryConfig);
            addErrorPages(context, jaggeryConfig);
            addSecurityConstraints(context, jaggeryConfig);
            setLoginConfig(context, jaggeryConfig);
            addSecurityRoles(context, jaggeryConfig);
            addParameters(context, jaggeryConfig);
            addLogLevel(context, jaggeryConfig);
        }
    }

    private static void addWelcomeFiles(Context context, JSONObject obj) {
        if(obj != null) {
            JSONArray arr = (JSONArray) obj.get(JaggeryConstants.JaggeryConfigParams.WELCOME_FILES);
            if(arr != null) {
                for(Object role : arr) {
                    context.addWelcomeFile((String)role);
                }
            } else {
                context.addWelcomeFile("index.jag");
                context.addWelcomeFile("index.html");
            }
        } else {
            context.addWelcomeFile("index.jag");
            context.addWelcomeFile("index.html");
        }
    }

    private static void addParameters(Context context, JSONObject object) {
        if(object != null) {
            Iterator<?> keys = object.keySet().iterator();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if(object.get(key) instanceof String) {
                    context.addParameter(key, (String) object.get(key));
                }
            }
        }
    }

    private static void addSecurityRoles(Context context, JSONObject object) {
        JSONArray arr = (JSONArray) object.get(JaggeryConstants.JaggeryConfigParams.SECURITY_ROLES);
        if(arr != null) {
            for(Object role : arr) {
                context.addSecurityRole((String) role);
            }
        }
    }

    private static void setDisplayName(Context context, JSONObject object) {
        if(object == null) {
            return;
        }
        String displayName = (String) object.get(JaggeryConstants.JaggeryConfigParams.DISPLAY_NAME);
        if(displayName != null) {
            context.setDisplayName(displayName);
        }
    }

    private static void addLogLevel(Context context, JSONObject object) {
        String level = (String) object.get(JaggeryConstants.JaggeryConfigParams.LOG_LEVEL);
        if(level == null) {
            return;
        }
        ServletContext servletContext = context.getServletContext();
        servletContext.setAttribute();
    }

    private static void setLoginConfig(Context context, JSONObject object) {
        JSONObject loginObject = (JSONObject) object.get(JaggeryConstants.JaggeryConfigParams.LOGIN_CONFIG);
        if(loginObject != null) {
            if(loginObject.get(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_FORM)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_FORM);
                loginConfig.setLoginPage((String) ((JSONObject) loginObject.get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_PAGE));
                loginConfig.setErrorPage((String) ((JSONObject) loginObject.get(JaggeryConstants.JaggeryConfigParams.FORM_LOGIN_CONFIG)).get(JaggeryConstants.JaggeryConfigParams.FORM_ERROR_PAGE));
                context.setLoginConfig(loginConfig);
            } else if(loginObject.get(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD).equals(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_BASIC)) {
                LoginConfig loginConfig = new LoginConfig();
                loginConfig.setAuthMethod(JaggeryConstants.JaggeryConfigParams.AUTH_METHOD_BASIC);
                context.setLoginConfig(loginConfig);
            }
        }
    }

    private static void addErrorPages(Context context, JSONObject object) {
        JSONObject arr = (JSONObject) object.get(JaggeryConstants.JaggeryConfigParams.ERROR_PAGES);
        if (arr != null) {
            for(Object keys : arr.keySet()) {
                ErrorPage errorPage = new ErrorPage();
                errorPage.setErrorCode((String)keys);
                errorPage.setLocation((String)arr.get(keys));
                context.addErrorPage(errorPage);
            }
        }
    }

    private static void addSecurityConstraints(Context context, JSONObject object) {
        JSONArray arr = (JSONArray) object.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINTS);
        if(arr != null) {
            for(Object sc : arr) {
                JSONObject jsonObject = (JSONObject) sc;
                SecurityConstraint securityConstraint = new SecurityConstraint();
                if(((JSONObject)jsonObject.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).
                        get(JaggeryConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION) != null) {
                    JSONObject resCollection = (JSONObject) ((JSONObject) jsonObject.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.WEB_RESOURCE_COLLECTION);
                    SecurityCollection securityCollection = new SecurityCollection();
                    securityCollection.setName((String) resCollection.get(JaggeryConstants.JaggeryConfigParams.WEB_RES_NAME));

                    JSONArray arrPattern = (JSONArray) resCollection.get(JaggeryConstants.JaggeryConfigParams.URL_PATTERNS);
                    for(Object anArrPattern : arrPattern) {
                        securityCollection.addPattern((String) anArrPattern);
                    }

                    JSONArray methods = (JSONArray) resCollection.get(JaggeryConstants.JaggeryConfigParams.HTTP_METHODS);
                    if(methods != null) {
                        for(Object method : methods) {
                            securityCollection.addMethod((String) method);
                        }
                    }

                    securityConstraint.addCollection(securityCollection);
                }

                if (((JSONObject) jsonObject.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.AUTH_ROLES) != null) {
                    JSONArray roles = (JSONArray) ((JSONObject) jsonObject.get(JaggeryConstants.JaggeryConfigParams.SECURITY_CONSTRAINT)).get(JaggeryConstants.JaggeryConfigParams.AUTH_ROLES);
                    for (Object role : roles) {
                        securityConstraint.addAuthRole((String) role);
                    }
                    securityConstraint.setAuthConstraint(true);
                }

                context.addConstraint(securityConstraint);
            }
        }
    }

    private JSONObject readJaggeryConfig(File f) throws IOException {
        File configFile = new File(f.getAbsolutePath() + File.separator + JaggeryConstants.JAGGERY_CONF_FILE);

        if(!configFile.exists()) {
            return null;
        }

        String jsonString = "";
        if(!configFile.isDirectory()) {
            FileInputStream fis = new FileInputStream(configFile);
            StringWriter writer = new StringWriter();
            IOUtils.copy(fis, writer, false);
            jsonString = writer.toString();
        }

        return (JSONObject) JSONValue.parse(jsonString);
    }
}
