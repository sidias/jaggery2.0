package org.jaggeryjs.scriptengine.parser;

import org.jaggeryjs.scriptengine.exception.ScriptException;

import java.io.*;

public class JaggeryParser {

    /**
     * Main Parser to process the .jss script
     *
     * @param stream script as the input stream
     * @throws ScriptException If an error occurred during the script parsing
     */
    public static InputStream parse(InputStream stream) throws ScriptException {
        try {
            boolean opened = false;
            boolean isExpression = false;
            String str;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream source = new PrintStream(output);
            StringBuilder html = new StringBuilder();
            StringBuilder jsExp = new StringBuilder();
            Reader inputReader = new InputStreamReader(stream,"utf-8");
            int ch =  inputReader.read();
            while (ch != -1) {
                if (ch == '<') {
                    ch = inputReader.read();
                    if (ch == '%') {
                        opened = true;
                        str = html.toString();
                        //as it is html, we can avoid adding empty print("") calls
                        if (!str.equals("")) {
                            source.append("print(\"").append(str).append("\");");
                            html = new StringBuilder();
                        }
                        ch = inputReader.read();
                        if (ch == '=') {
                            isExpression = true;
                        } else {
                            continue;
                        }
                    } else {
                        if (opened) {
                            if (isExpression) {
                                jsExp.append("<");
                            } else {
                                source.append("<");
                            }
                        } else {
                            html.append('<');
                        }
                        continue;
                    }
                    ch = inputReader.read();
                } else if (ch == '%') {
                    ch = inputReader.read();
                    if (ch == '>') {
                        opened = false;
                        if (isExpression) {
                            isExpression = false;
                            //if it need, we can validate "jsExp" here or let the compiler to do it.
                            source.append("print(").append(jsExp).append(");");
                            jsExp = new StringBuilder();
                        }
                    } else {
                        if (opened) {
                            source.append('%');
                        } else {
                            html.append('%');
                        }
                        continue;
                    }
                    ch = inputReader.read();
                } else {
                    if (opened) {
                        if (isExpression) {
                            jsExp.append((char) ch);
                        } else {
                            source.append((char) ch);
                        }
                        ch = inputReader.read();
                    } else {
                        int next = inputReader.read();
                        if (ch == '"') {
                            html.append('\\').append('\"');
                        } else if (ch == '\\') {
                            html.append('\\').append('\\');
                        } else if (ch == '\r') {
                            html.append('\\').append('r');
                        } else if (ch == '\n') {
                            source.append("print(\"").append(html.toString());
                            if (next != -1) {
                                source.append('\\').append('n');
                            }
                            source.append("\");").append('\n');
                            html = new StringBuilder();
                        } else if (ch == '\t') { // Not sure we need this
                            html.append('\\').append('t');
                        } else {
                            html.append((char) ch);
                        }
                        ch = next;
                    }
                }
            }
            str = html.toString();
            if (!str.equals("")) {
                source.append("print(\"").append(str).append("\");");
            }
            str = jsExp.toString();
            if (!str.equals("")) {
                source.append("print(").append(str).append(");");
            }
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }
}
