package com.sebuilder.interpreter.script;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Aspect;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import com.sebuilder.interpreter.TestRunListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.Map;

public class SeleniumIDE extends AbstractJsonScriptParser {

    private static Logger logger = LogManager.getLogger(SeleniumIDE.class);

    @Override
    public TestCase load(String jsonString) {
        try {
            return this.parseTest(new JSONObject(new JSONTokener(jsonString)));
        } catch (JSONException e) {
            throw new AssertionError("error parse:" + jsonString, e);
        }
    }

    @Override
    public Aspect loadAspect(File f) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected TestCase load(JSONObject o, File sourceFile, TestRunListener testRunListener) throws JSONException {
        if (!o.has("tests")) {
            return new TestCaseBuilder().build();
        }
        TestCaseBuilder builder = TestCaseBuilder.suite(null)
                .setName(o.getString("name"));
        JSONArray tests = o.getJSONArray("tests");
        for (int i = 0, j = tests.length(); i < j; i++) {
            builder.addChain(this.parseTest(tests.getJSONObject(i)));
        }
        return builder.build();
    }

    private TestCase parseTest(JSONObject test) throws JSONException {
        TestCaseBuilder builder = new TestCaseBuilder()
                .setName(test.getString("name") + ".json");
        JSONArray commands = test.getJSONArray("commands");
        for (int i = 0, j = commands.length(); i < j; i++) {
            Command command = new Command(commands.getJSONObject(i));
            if (!SeleniumIDECommandToStep.support(command)) {
                logger.error("command {} is currently not supported", command.command());
                continue;
            }
            builder.addStep(SeleniumIDECommandToStep.convert(command));
        }
        return builder.build();
    }

    static class Command {
        private final JSONObject source;

        public Command(JSONObject source) {
            this.source = source;
        }

        public String command() throws JSONException {
            return this.source.get("command").toString();
        }

        public String target() throws JSONException {
            return this.source.get("target").toString();
        }

        public String value() throws JSONException {
            return this.source.get("value").toString();
        }

        public Map<String, String> targets() throws JSONException {
            Map<String, String> result = Maps.newHashMap();
            JSONArray targets = this.source.getJSONArray("targets");
            for (int i = 0, j = targets.length(); i < j; i++) {
                JSONArray locator = targets.getJSONArray(i);
                result.put(locator.getString(1), locator.getString(0));
            }
            return result;
        }
    }
}
