package com.sebuilder.interpreter.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.datasource.Manual;
import com.sebuilder.interpreter.datasource.None;
import com.sebuilder.interpreter.report.JunitTestRunListener;
import com.sebuilder.interpreter.step.Loop;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import com.sebuilder.interpreter.step.getter.ElementAttribute;
import com.sebuilder.interpreter.step.type.ClickElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class SebuilderTest {

    static {
        Context.getInstance()
                .setStepTypeFactory(new StepTypeFactoryImpl())
                .setDataSourceFactory(new DataSourceFactoryImpl());
    }

    static Logger log = LogManager.getLogger(SebuilderTest.class);
    private static final String baseDir = Objects.requireNonNull(SebuilderTest.class.getResource(".")).getPath();
    private static final Sebuilder target = new Sebuilder();
    private static final SebuilderToStringConverter toStringConverter = new SebuilderToStringConverter();

    private static final DataSourceLoader DATA_SET_NONE = new DataSourceLoader(new None(), Map.of(), new File(baseDir));
    private static final DataSourceLoader DATA_SET_CSV = new DataSourceLoader(new Csv(), Map.of("path", "test.csv"), new File(baseDir));
    private static final DataSourceLoader DATA_SET_OVERRIDE_CSV = new DataSourceLoader(new Csv(), Map.of("path", "override.csv"), new File(baseDir));
    private static final DataSourceLoader DATA_SET_OVERRIDE_LAZY = new DataSourceLoader(new Csv(), Map.of("path", "${dataSource1}.csv"), null);

    private static final File testFileNoType = new File(baseDir, "noType.json");
    private static final File testFileScriptWithNoContents = new File(baseDir, "scriptWithNoContents.json");
    private static final File testFileScriptWithDataSource = new File(baseDir, "scriptWithDataSource.json");
    private static final File testFileScriptWithSteps = new File(baseDir, "scriptWithSteps.json");
    private static final File testFileScriptWithFullContents = new File(baseDir, "scriptWithFullContents.json");

    public static class StepLoadTest {
        @Test
        public void parseStep() throws FileNotFoundException, JSONException {
            List<Step> results = target.parseStep(new JSONObject(new JSONTokener(new FileReader(testFileScriptWithSteps))));
            assertEquals(10, results.size());
            assertStep(results, 0, new ClickElement(), Locator.Type.ID, "id1", false, false);

            assertStep(results, 1, new ClickElement(), Locator.Type.NAME, "name1", true, false);
            assertStep(results, 2, new ElementAttribute().toWaitFor(), Locator.Type.LINK_TEXT, "link", false, false);
            assertStep(results, 3, new ElementAttribute().toVerify(), Locator.Type.CSS_SELECTOR, "selector", false, false);
            assertStep(results, 4, new ElementAttribute().toAssert(), Locator.Type.XPATH, "//", false, false);
            assertStep(results, 5, new ElementAttribute().toStore(), Locator.Type.CSS_SELECTOR, "selector", false, true);
            assertStep(results, 6, new ElementAttribute().toPrint(), Locator.Type.XPATH, "//", false, false);
            assertStep(results, 7, new ElementAttribute().toIf(), Locator.Type.ID, "id1", false, false);
            assertStep(results, 8, new ElementAttribute().toRetry(), Locator.Type.NAME, "name1", false, false);
            assertEquals(results.get(9).type(), new Loop());
        }

        private void assertStep(List<Step> results, int i, StepType stepType, Locator.Type locatorName, String locatorValue, boolean isSkip, boolean isNageted) {
            assertEquals(results.get(i).type(), stepType);
            assertEquals(results.get(i).getLocator("locator").type(), locatorName.toString());
            assertEquals(results.get(i).getLocator("locator").value(), locatorValue);
            assertEquals(results.get(i).isSkip(new InputData()), isSkip);
            assertEquals(results.get(i).negated(), isNageted);
        }
    }

    public static abstract class ParseResultTest {
        protected TestCase result;
        protected TestRunListener testRunListener = new JunitTestRunListener(log);

        @Test
        public void parseResultContents() {
            this.getTestCaseAssert().run(result);
        }

        @Test
        public void parseResultReversible() throws IOException {
            TestCase reverse = target.load(toStringConverter.toString(this.result), Strings.isNullOrEmpty(this.result.path()) ? null : new File(this.result.path()));
            this.getTestCaseAssert().run(reverse);
        }

        public abstract TestCaseAssert getTestCaseAssert();

    }

    public static class ParseNoType extends ParseResultTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileNoType);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileNoType))
                    .assertStep(TestCaseAssert::assertEqualsNoStep)
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                    .assertOverrideDataSource(TestCaseAssert::assertEqualsNoOverrideDataSource)
                    .assertLazy(TestCaseAssert::assertNotLazyLoad)
                    .assertNestedChain(TestCaseAssert::assertNotNestedChain)
                    .create();
        }
    }

    public static class ParseScriptNoContents extends ParseResultTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithNoContents);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithNoContents))
                    .create();
        }

    }

    public static class ParseScriptTypeWithDataSource extends ParseResultTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithDataSource);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithDataSource))
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(DATA_SET_CSV))
                    .create();
        }
    }

    public static class ParseScriptTypeWithSteps extends ParseResultTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithSteps);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithSteps))
                    .assertStep(TestCaseAssert.assertEqualsStepCount(10))
                    .create();
        }
    }

    public static class ParseScriptTypeWithFullContents extends ParseResultTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithFullContents);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithFullContents))
                    .assertStep(TestCaseAssert.assertEqualsStepCount(9))
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(DATA_SET_CSV))
                    .create();
        }
    }

    public static class ParseSuiteNoContents extends ParseResultTest {

        private final File testFile = new File(baseDir, "simpleSuite.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .create();
        }
    }

    public static class ParseSuiteWithDataSource extends ParseResultTest {


        private final File testFile = new File(baseDir, "suiteWithDataSource.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(DATA_SET_CSV))
                    .create();
        }
    }

    public static class ParseSuiteWithScripts extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithScripts.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(3))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_CSV))
                            .create())
                    .assertChainCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertChainCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .create();
        }
    }

    public static class ParseSuiteWithScriptChain extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithScriptChain.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_CSV))
                            .assertChainCaseCounts(2)
                            .assertChainCase(0, new ParseScriptTypeWithSteps().getTestCaseAssert()
                                    .builder()
                                    .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                                    .create())
                            .assertChainCase(1, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                                    .builder()
                                    .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                                    .create())
                            .create())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .create();
        }
    }

    public static class ParseSuiteWithScriptChainContainsSameScript extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithScriptChainContainsSameScript.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(3))
                            .assertChainCase(0, new ParseScriptTypeWithSteps().getTestCaseAssert())
                            .assertChainCase(1, new ParseScriptNoContents().getTestCaseAssert()
                                    .builder()
                                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("scriptWithNoContents.json", testFileScriptWithNoContents))
                                    .create())
                            .assertChainCase(2, new ParseScriptTypeWithSteps().getTestCaseAssert()
                                    .builder()
                                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("scriptWithSteps.json(1)", testFileScriptWithSteps))
                                    .create())
                            .create())
                    .create();
        }
    }

    public static class ParseSuiteWithNestedScriptChain extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithNestedScriptChain.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_CSV))
                            .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                            .assertChainCase(0, new ParseScriptTypeWithSteps().getTestCaseAssert()
                                    .builder()
                                    .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                                    .assertNestedChain(TestCaseAssert::assertNestedChain)
                                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                                    .assertChainCase(0, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                                            .builder()
                                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                                            .create())
                                    .create())
                            .create())
                    .create();
        }
    }

    public static class ParseSuiteWithScriptRuntimeDataSource extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithScriptRuntimeDataSource.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(3))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(new DataSourceLoader(new Csv(), Map.of("path", "${path}/override.csv"), new File(baseDir))))
                            .create())
                    .assertChainCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertChainCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .create();
        }
    }

    public static class ParseSuiteWithLazyLoad extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithLazyScript.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(3))
                    .assertChainCase(0, TestCaseAssert.of()
                            .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("${script1}.json"))
                            .assertSkip(TestCaseAssert.assertEqualsSkip("${skip1}"))
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_LAZY))
                            .assertLazy(TestCaseAssert::assertLazyLoad)
                            .create())
                    .assertChainCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertChainCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(new DataSourceLoader(new Manual()
                            , Map.of("script1", "scriptWithNoContents", "skip1", "false", "dataSource1", "override")
                            , new File(baseDir))))
                    .create();
        }
    }

    public static class ParseSuiteWithLazyScriptChain extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithLazyScriptChain.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                    .assertChainCase(0, TestCaseAssert.of()
                            .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("${script1}.json"))
                            .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                            .assertLazy(TestCaseAssert::assertLazyLoad)
                            .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(2))
                            .assertChainCase(0, new ParseScriptTypeWithSteps().getTestCaseAssert()
                                    .builder()
                                    .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                                    .create())
                            .assertChainCase(1, TestCaseAssert.of()
                                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("${script3}.json"))
                                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                                    .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(new DataSourceLoader(new Csv(), Map.of("path", "${dataSource3}.csv"), null)))
                                    .assertLazy(TestCaseAssert::assertLazyLoad)
                                    .create())
                            .create())
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(new DataSourceLoader(new Manual()
                            , Map.of("script1", "scriptWithDataSource", "script3", "scriptWithNoContents", "dataSource3", "csv/override")
                            , new File(baseDir))))
                    .create();
        }
    }

    public static class ParseSuiteWithImportSuite extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithImportSuite.json");

        @Before
        public void setUp() throws IOException {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(2))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert())
                    .assertChainCase(1, new ParseSuiteWithScripts().getTestCaseAssert())
                    .create();
        }
    }
}