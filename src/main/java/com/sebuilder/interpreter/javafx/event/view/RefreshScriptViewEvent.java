package com.sebuilder.interpreter.javafx.event.view;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Suite;

import java.util.LinkedHashMap;

public class RefreshScriptViewEvent {

    private final String fileName;

    private final LinkedHashMap<String, Script> scripts = Maps.newLinkedHashMap();

    private final String selectScriptName;

    public RefreshScriptViewEvent(Suite suite, String selectScriptName) {
        this.fileName = suite.getName();
        this.selectScriptName = selectScriptName;
        for (Script script : suite) {
            this.scripts.put(script.name, script);
        }
    }


    public String getFileName() {
        return fileName;
    }

    public LinkedHashMap<String, Script> getScripts() {
        return Maps.newLinkedHashMap(this.scripts);
    }

    public String getSelectScriptName() {
        return selectScriptName;
    }
}
