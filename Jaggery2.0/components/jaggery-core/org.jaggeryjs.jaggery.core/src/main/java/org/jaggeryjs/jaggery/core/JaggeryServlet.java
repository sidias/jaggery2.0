package org.jaggeryjs.jaggery.core;

import org.jaggeryjs.jaggery.core.manager.WebAppManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.exception.ScriptException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JaggeryServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(JaggeryServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WebAppManager.execute(request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WebAppManager.execute(request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WebAppManager.execute(request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WebAppManager.execute(request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WebAppManager.execute(request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WebAppManager.execute(request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    protected void doTrace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            WebAppManager.execute(request, response);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
