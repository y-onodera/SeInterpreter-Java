package com.sebuilder.interpreter.javafx.event.script;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.factory.ScriptFactory;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RefreshScriptViewEvent {

    private final String fileName;

    private final List<Script> scripts;

    public RefreshScriptViewEvent(File targetFile) throws IOException, JSONException {
        if (targetFile != null) {
            this.fileName = targetFile.getName();
            this.scripts = new ScriptFactory().parse(targetFile);
        } else {
            this.fileName = "new script";
            this.scripts = Lists.newArrayList(new ScriptFactory().parse("{\"steps\":[" + "{\"type\":\"get\",\"url\":\"https://www.google.com\"}" + "]}"));
        }
    }

    public String getFileName() {
        return fileName;
    }

    public List<Script> getScripts() {
        return Lists.newArrayList(this.scripts);
    }

}
