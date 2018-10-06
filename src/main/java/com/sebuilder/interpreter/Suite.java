package com.sebuilder.interpreter;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.factory.ScriptFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class Suite implements Iterable<Script> {

    private String name;

    private String path;

    private LinkedHashMap<String, Script> scripts = Maps.newLinkedHashMap();

    private final boolean shareState;

    public Suite() {
        this(null, new ArrayList<>(), true);
    }

    public Suite(Iterable<Script> aScripts, boolean shareState) {
        this(null, aScripts, shareState);
    }

    public Suite(File file, ScriptFactory scriptFactory) throws IOException, JSONException {
        this(file, scriptFactory.parse(file), true);
    }

    public Suite(File suiteFile, Iterable<Script> aScripts, boolean shareState) {
        if (suiteFile != null) {
            this.name = suiteFile.getName();
            this.path = suiteFile.getAbsolutePath();
        }
        aScripts.forEach(it -> scripts.put(it.name, it));
        this.shareState = shareState;
    }

    public String getName() {
        return this.name;
    }

    public Script get(String scriptName) {
        return this.scripts.get(scriptName);
    }

    @Override
    public Iterator<Script> iterator() {
        return this.scripts.values().iterator();
    }

    public void add(Script aScript) {
        this.scripts.put(aScript.name, aScript);
    }

    public void replace(Script aScript) {
        this.scripts.replace(aScript.name, aScript);
    }

    public void replace(String oldName, Script newValue) {
        LinkedHashMap<String, Script> newMap = Maps.newLinkedHashMap();
        this.scripts.entrySet().forEach(it -> {
            if (it.getKey().equals(oldName)) {
                newMap.put(newValue.name, newValue);
            } else {
                newMap.put(it.getKey(), it.getValue());
            }
        });
        this.scripts.clear();
        this.scripts.putAll(newMap);
    }

    @Override
    public String toString() {
        try {
            return toJSON().toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        JSONArray scriptsA = new JSONArray();
        for (Script s : this.scripts.values()) {
            JSONObject scriptPath = new JSONObject();
            scriptPath.put("path", s.path);
            scriptsA.put(scriptPath);
        }
        o.put("type", "suite");
        o.put("scripts", scriptsA);
        o.put("shareState", this.shareState);
        return o;
    }

}
