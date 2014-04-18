package org.jaggeryjs.scriptengine.exception;


public class ScriptException extends Exception {

    public ScriptException(String msg) {
        super(msg);
    }

    public ScriptException(Exception exception) {
        super(exception);
    }

    public ScriptException(String msg, Exception exception) {
        super(msg, exception);
    }
}
