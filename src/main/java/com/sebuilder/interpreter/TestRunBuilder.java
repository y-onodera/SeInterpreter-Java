package com.sebuilder.interpreter;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.factory.TestRunFactory;
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

    public String name() {
        return this.script.name().substring(0, script.name.indexOf('.'));
    }

    public boolean closeDriver() {
        return this.script.closeDriver();
    }

    public TestRun createTestRun(TestRunFactory testRunFactory, Logger log, WebDriverFactory wdf, HashMap<String, String> driverConfig, Map<String, String> data, TestRun lastRun, SeInterpreterTestListener seInterpreterTestListener) {
        return testRunFactory.createTestRun(this.script.copy(), log, wdf, driverConfig, data, lastRun, seInterpreterTestListener, this.scriptChain);
    }

}
