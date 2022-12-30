package com.sebuilder.interpreter.script.seleniumide;

import com.google.common.collect.ImmutableMap;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public SeleniumIDEConverter(final JSONObject source) {
        this.source = source;
    }

    public SeleniumIDEConverter() {
        this(null);
    }

    public String getUrl() {
        return this.url;
    }

    public TestCase parseTest(final JSONObject test) {
        final TestCaseBuilder builder = new TestCaseBuilder()
                .setName(test.getString("name") + ".json");
        final JSONArray commands = test.getJSONArray("commands");
        IntStream.range(0, commands.length())
                .mapToObj(i -> new SeleniumIDECommand(commands.getJSONObject(i)))
                .filter(command -> {
                    if (!this.support(command)) {
                        logger.error("command {} is currently not supported", command.command());
                        return false;
                    }
                    return true;
                }).forEach(command -> builder.addStep(this.convert(command)));
        return builder.build();
    }

    public TestCase getResult() {
        if (!this.source.has("tests")) {
            return new TestCaseBuilder().build();
        }
        this.url = this.source.getString("url");
        final TestCaseBuilder builder = TestCaseBuilder.suite(null)
                .setName(this.source.getString("name"));
        final JSONArray suites = this.source.getJSONArray("suites");
        IntStream.range(0, suites.length()).forEach(i -> builder.addChain(this.parseSuite(suites.getJSONObject(i))));
        return builder.build();
    }

    private TestCase parseSuite(final JSONObject suite) {
        final List<String> testIdList = this.getTestIdList(suite);
        final JSONArray tests = this.source.getJSONArray("tests");
        final TestCaseBuilder suiteBuilder = TestCaseBuilder.suite(null)
                .setName(suite.getString("name") + ".json");
        IntStream.range(0, tests.length())
                .mapToObj(tests::getJSONObject)
                .filter(test -> testIdList.contains(test.getString("id")))
                .forEach(test -> suiteBuilder.addChain(this.parseTest(test)));
        return suiteBuilder.build();
    }

    private List<String> getTestIdList(final JSONObject suite) {
        final JSONArray testIds = suite.getJSONArray("tests");
        return IntStream.range(0, testIds.length())
                .mapToObj(testIds::getString)
                .collect(Collectors.toList());
    }

    private boolean support(final SeleniumIDECommand command) {
        return CONVERT_MAP.containsKey(command.command());
    }

    private Step convert(final SeleniumIDECommand command) {
        return CONVERT_MAP.get(command.command()).toStep(this, command);
    }

}
