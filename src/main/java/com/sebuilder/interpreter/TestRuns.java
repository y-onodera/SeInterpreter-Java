package com.sebuilder.interpreter;

import org.openqa.selenium.Keys;

import java.util.Map;

public enum TestRuns {
    SINGLETON;

    public static String replaceVariable(String s, Map<String, String> vars) {
        // Sub special keys using the !{keyname} syntax.
        s = replaceKeys(s);
        // This kind of variable substitution makes for short code, but it's inefficient.
        s = replaceVars(s, vars);
        return s;
    }

    public static String replaceKeys(String s) {
        for (Keys k : Keys.values()) {
            s = s.replace("!{" + k.name() + "}", k.toString());
        }
        return s;
    }

    public static String replaceVars(String variable, Map<String, String> vars) {
        for (Map.Entry<String, String> v : vars.entrySet()) {
            variable = variable.replace("${" + v.getKey() + "}", v.getValue());
        }
        return variable;
    }

}
