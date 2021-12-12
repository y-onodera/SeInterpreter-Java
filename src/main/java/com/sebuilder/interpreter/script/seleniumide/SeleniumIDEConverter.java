package com.sebuilder.interpreter.script.seleniumide;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SeleniumIDEConverter {

    private static final Logger logger = LogManager.getLogger(SeleniumIDEConverter.class);
    private static final ImmutableMap<String, StepConverter> CONVERT_MAP = new ImmutableMap.Builder<String, StepConverter>()
            .put("open", new GetConverter())
            .put("close", new CloseWindowConverter())
            .put("setWindowSize", new SetWindowSizeConverter())
            .put("type", new SetElementTextConverter())
            .put("sendKeys", new SendKeysToElementConverter())
            .put("select", new SetElementSelectedConverter(SetElementSelectedConverter.Input.SELECT))
            .put("check", new SetElementSelectedConverter(SetElementSelectedConverter.Input.CHECK))
            .put("uncheck", new SetElementNotSelectedConverter())
            .put("click", new ClickElementConverter())
            .put("clickAt", new ClickElementAtConverter())
            .put("doubleClick", new DoubleClickElementConverter())
            .put("doubleClickAt", new DoubleClickElementAtConverter())
            .put("mouseDown", new ClickAndHoldElementConverter())
            .put("mouseUp", new ReleaseElementConverter())
            .put("mouseOver", new MouseOverElementConverter())
            .put("assert", new AssertVariableConverter())
            .put("webdriverAnswerOnVisiblePrompt", new AnswerAlertConverter())
            .put("webdriverChooseCancelOnVisiblePrompt", new DismissAlertConverter())
            .put("webdriverChooseOkOnVisibleConfirmation", new AcceptAlertConverter())
            .put("webdriverChooseCancelOnVisibleConfirmation", new DismissAlertConverter())
            .put("selectWindow", new SwitchToWindowConverter())
            .put("selectFrame", new SwitchToFrameConverter())
            .build();

    private String url;
    private final JSONObject source;

    public SeleniumIDEConverter(JSONObject source) {
        this.source = source;
    }

    public SeleniumIDEConverter() {
        this(null);
    }

    public String getUrl() {
        return this.url;
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
        return CONVERT_MAP.get(command.command()).toStep(this, command);
    }

}
