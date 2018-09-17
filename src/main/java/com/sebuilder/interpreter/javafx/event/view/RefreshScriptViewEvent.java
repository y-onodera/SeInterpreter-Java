package com.sebuilder.interpreter.javafx.event.view;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Script;

import java.util.LinkedHashMap;
import java.util.Map;

public class RefreshScriptViewEvent {

    private final String fileName;

    private final LinkedHashMap<String, Script> scripts = Maps.newLinkedHashMap();

    public RefreshScriptViewEvent(String fileName, LinkedHashMap<String, Script> scripts) {
        this.fileName = fileName;
        for (Map.Entry<String, Script> entry : scripts.entrySet()) {
            this.scripts.put(entry.getKey(), entry.getValue());
        }
    }

    public String getFileName() {
        return fileName;
    }

    public LinkedHashMap<String, Script> getScripts() {
        return Maps.newLinkedHashMap(this.scripts);
    }

}
