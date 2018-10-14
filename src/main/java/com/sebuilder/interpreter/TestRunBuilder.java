package com.sebuilder.interpreter;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestRunBuilder {

    private final Script script;
    private final Map<Script, Script> scriptChain;

    public TestRunBuilder(Script script) {
        this.script = script;
        this.scriptChain = Maps.newHashMap();
    }

    public String getScriptName() {
        return this.getScriptFileName().substring(0, this.script.name.indexOf('.'));
    }

    public String getScriptFileName() {
        return this.script.name();
    }

    public boolean closeDriver() {
        return this.script.closeDriver();
    }

    public TestRunBuilder addChain(Map<Script, Script> scriptChain) {
        Script scriptFrom = this.script;
        while (scriptChain.containsKey(scriptFrom)) {
            Script chainTo = scriptChain.get(scriptFrom);
            this.scriptChain.put(scriptFrom, chainTo);
            scriptFrom = chainTo;
        }
        return this;
    }

    public List<Map<String, String>> loadData() {
        return this.script.loadData();
    }

    public TestRun createTestRun(Logger log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, Long implicityWaitTime, Long pageLoadWaitTime, Map<String, String> initialVars, TestRun previousRun, SeInterpreterTestListener seInterpreterTestListener) {
        final Script copy = this.script.copy();
        final Map<Script, Script> copyScriptChain = Maps.newHashMap();
        for (Map.Entry<Script, Script> entry : this.scriptChain.entrySet()) {
            if (entry.getKey() == this.script) {
                copyScriptChain.put(copy, entry.getValue());
            } else if (entry.getValue() == this.script) {
                copyScriptChain.put(entry.getKey(), copy);
            } else {
                copyScriptChain.put(entry.getKey(), entry.getValue());
            }
        }
        if (this.script.usePreviousDriverAndVars && previousRun != null && previousRun.driver() != null) {
            return new TestRun(copy, log, previousRun.driver(), implicityWaitTime, pageLoadWaitTime, initialVars, seInterpreterTestListener, copyScriptChain);
        }
        return new TestRun(copy, log, webDriverFactory, webDriverConfig, implicityWaitTime, pageLoadWaitTime, initialVars, seInterpreterTestListener, copyScriptChain);
    }
}
