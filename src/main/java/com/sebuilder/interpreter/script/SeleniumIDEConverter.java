package com.sebuilder.interpreter.script;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.step.LocatorHolder;
import com.sebuilder.interpreter.step.type.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

public class SeleniumIDEConverter {

    private static Logger logger = LogManager.getLogger(SeleniumIDEConverter.class);
    private static final ImmutableMap<String, StepType> CONVERT_MAP = new ImmutableMap.Builder<String, StepType>()
            .put("open", new Get())
            .put("click", new ClickElement())
            .put("clickAt", new ClickElementAt())
            .put("doubleClick", new DoubleClickElement())
            .put("doubleClickAt", new DoubleClickElementAt())
            .put("type", new SetElementText())
            .put("sendKeys", new SendKeysToElement())
            .put("select", new SetElementSelected())
            .put("selectFrame", new SwitchToFrame())
            .put("selectWindow", new SwitchToWindow())
            .put("setWindowSize", new SetWindowSize())
            .build();

    private String url;
    private JSONObject source;

    public SeleniumIDEConverter(JSONObject source) {
        this.source = source;
    }

    public SeleniumIDEConverter() {
        this(null);
    }

    public TestCase parseTest(JSONObject test) throws JSONException {
        TestCaseBuilder builder = new TestCaseBuilder()
                .setName(test.getString("name") + ".json");
        JSONArray commands = test.getJSONArray("commands");
        for (int i = 0, j = commands.length(); i < j; i++) {
            SeleniumIDECommand command = new SeleniumIDECommand(commands.getJSONObject(i));
            if (!this.support(command)) {
                logger.error("command {} is currently not supported", command.command());
                continue;
            }
            builder.addStep(this.convert(command));
        }
        return builder.build();
    }

    public TestCase getResult() throws JSONException {
        if (!this.source.has("tests")) {
            return new TestCaseBuilder().build();
        }
        this.url = this.source.getString("url");
        TestCaseBuilder builder = TestCaseBuilder.suite(null)
                .setName(this.source.getString("name"));
        JSONArray suites = this.source.getJSONArray("suites");
        for (int i = 0, j = suites.length(); i < j; i++) {
            JSONObject suite = suites.getJSONObject(i);
            builder.addChain(parseSuite(suite));
        }
        return builder.build();
    }

    private TestCase parseSuite(JSONObject suite) throws JSONException {
        List<String> testIdList = getTestIdList(suite);
        JSONArray tests = this.source.getJSONArray("tests");
        TestCaseBuilder suiteBuilder = TestCaseBuilder.suite(null)
                .setName(suite.getString("name") + ".json");
        for (int i = 0, j = tests.length(); i < j; i++) {
            JSONObject test = tests.getJSONObject(i);
            if (testIdList.contains(test.getString("id"))) {
                suiteBuilder.addChain(this.parseTest(test));
            }
        }
        return suiteBuilder.build();
    }

    private List<String> getTestIdList(JSONObject suite) throws JSONException {
        List<String> result = Lists.newArrayList();
        JSONArray testIds = suite.getJSONArray("tests");
        for (int i = 0, j = testIds.length(); i < j; i++) {
            result.add(testIds.getString(i));
        }
        return result;
    }

    private boolean support(SeleniumIDECommand command) throws JSONException {
        return CONVERT_MAP.containsKey(command.command());
    }

    private Step convert(SeleniumIDECommand command) throws JSONException {
        StepBuilder builder = new StepBuilder(CONVERT_MAP.get(command.command()));
        if (builder.getStepType() instanceof SetElementSelected) {
            String optionPath = String.format("/option[. = '%s']", command.value().replace("label=", ""));
            builder.locator(new Locator("xpath", targetToXpath(command) + optionPath));
        } else if (builder.getStepType() instanceof LocatorHolder) {
            String[] locatorTarget = command.target().split("=", 2);
            if (locatorTarget.length == 2) {
                builder.locator(new Locator(convertLocatorType(locatorTarget[0]), locatorTarget[1]));
            }
            if (builder.getStepType() instanceof SetElementText
                    || builder.getStepType() instanceof SendKeysToElement) {
                builder.getStringParams().put("text", command.value());
            } else if (builder.getStepType() instanceof ClickElementAt
                    || builder.getStepType() instanceof DoubleClickElementAt) {
                String[] point = command.value().split(",");
                if (point.length == 2) {
                    builder.getStringParams().put("pointX", point[0]);
                    builder.getStringParams().put("pointY", point[1]);
                }
            }
        } else if (builder.getStepType() instanceof SetWindowSize) {
            String[] size = command.target().split("x", 2);
            if (size.length == 2) {
                builder.getStringParams().put("width", size[0]);
                builder.getStringParams().put("height", size[1]);
            }
        } else if (builder.getStepType() instanceof Get) {
            if (Optional.ofNullable(command.target()).orElse("").startsWith("http")) {
                builder.getStringParams().put("url", command.target());
            } else {
                builder.getStringParams().put("url", this.url + "/" + command.target()
                        .replaceAll("(?<!:)/{2,9}", "/"));
            }
        }
        return builder.build();
    }

    private String convertLocatorType(String s) {
        return s.replace("css", "css selector")
                .replace("linkText", "link text");
    }

    private String targetToXpath(SeleniumIDECommand command) throws JSONException {
        String target = command.target();
        if (target.startsWith("id=")) {
            return String.format("//*[@id='%s']", target.replace("id=", ""));
        } else if (target.startsWith("name=")) {
            return String.format("//*[@name='%s']", target.replace("name=", ""));
        } else if (target.startsWith("css=")) {
            return String.format("//*[@css='%s']", target.replace("css=", ""));
        }
        return target.replace("xpath=", "");
    }

}
