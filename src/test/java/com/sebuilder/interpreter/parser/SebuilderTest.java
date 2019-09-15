package com.sebuilder.interpreter.parser;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.datasource.Manual;
import com.sebuilder.interpreter.datasource.None;
import com.sebuilder.interpreter.step.Loop;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import com.sebuilder.interpreter.step.getter.ElementAttribute;
import com.sebuilder.interpreter.step.type.ClickElement;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(Enclosed.class)
public class SebuilderTest {

    static {
        Context.getInstance()
                .setStepTypeFactory(new StepTypeFactoryImpl())
                .setDataSourceFactory(new DataSourceFactoryImpl());
    }

    private static String baseDir = SebuilderTest.class.getResource(".").getPath();
    private static Sebuilder target = new Sebuilder();

    private static final TestDataSet DATA_SET_NONE = new TestDataSet(new None(), Map.of(), new File(baseDir));
    private static final TestDataSet DATA_SET_CSV = new TestDataSet(new Csv(), Map.of("path", "test.csv"), new File(baseDir));
    private static final TestDataSet DATA_SET_OVERRIDE_CSV = new TestDataSet(new Csv(), Map.of("path", "override.csv"), new File(baseDir));
    private static final TestDataSet DATA_SET_OVERRIDE_LAZY = new TestDataSet(new Csv(), Map.of("path", "${dataSource1}.csv"), null);

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
            assertEquals(results.get(9).getType(), new Loop());
        }

        private void assertStep(List<Step> results, int i, StepType stepType, Locator.Type locatorName, String locatorValue, boolean isSkip, boolean isNageted) {
            assertEquals(results.get(i).getType(), stepType);
            assertSame(results.get(i).getLocator("locator").type, locatorName);
            assertEquals(results.get(i).getLocator("locator").value, locatorValue);
            assertEquals(results.get(i).isSkip(new TestData()), isSkip);
            assertEquals(results.get(i).isNegated(), isNageted);
        }
    }

    public static abstract class ParseResultTest {
        protected Suite result;

        @Test
        public void parseResultContents() {
            this.getSuiteAssert().run(result);
        }

        @Test
        public void parseResultReversible() throws IOException {
            Suite reverse = target.load(target.toString(this.result), Strings.isNullOrEmpty(this.result.getPath()) ? null : new File(this.result.getPath()));
            this.getSuiteAssert().run(reverse);
        }

        public abstract SuiteAssert getSuiteAssert();

    }

    public static abstract class ParseScriptTest extends ParseResultTest {

        public abstract TestCaseAssert getTestCaseAssert();

        public TestCaseAssert reverseTestCaseAssert() {
            return this.getTestCaseAssert();
        }
    }


    public static class ParseNoType extends ParseScriptTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileNoType);
        }

        @Override
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert::assertEqualsNoRelationFile)
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(1))
                    .assertTestCase(0, this.getTestCaseAssert())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
                    .create();
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

    public static class ParseScriptNoContents extends ParseScriptTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithNoContents);
        }

        @Override
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert::assertEqualsNoRelationFile)
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(1))
                    .assertTestCase(0, this.getTestCaseAssert())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
                    .create();
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithNoContents))
                    .assertStep(TestCaseAssert::assertEqualsNoStep)
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                    .assertOverrideDataSource(TestCaseAssert::assertEqualsNoOverrideDataSource)
                    .assertLazy(TestCaseAssert::assertNotLazyLoad)
                    .assertNestedChain(TestCaseAssert::assertNotNestedChain)
                    .create();
        }

    }

    public static class ParseScriptTypeWithDataSource extends ParseScriptTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithDataSource);
        }

        @Override
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert::assertEqualsNoRelationFile)
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(1))
                    .assertTestCase(0, this.getTestCaseAssert())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
                    .create();
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithDataSource))
                    .assertStep(TestCaseAssert::assertEqualsNoStep)
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(DATA_SET_CSV))
                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                    .assertOverrideDataSource(TestCaseAssert::assertEqualsNoOverrideDataSource)
                    .assertLazy(TestCaseAssert::assertNotLazyLoad)
                    .assertNestedChain(TestCaseAssert::assertNotNestedChain)
                    .create();
        }
    }

    public static class ParseScriptTypeWithSteps extends ParseScriptTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithSteps);
        }

        @Override
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert::assertEqualsNoRelationFile)
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(1))
                    .assertTestCase(0, this.getTestCaseAssert())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
                    .create();
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithSteps))
                    .assertStep(TestCaseAssert.assertEqualsStepCount(10))
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                    .assertOverrideDataSource(TestCaseAssert::assertEqualsNoOverrideDataSource)
                    .assertLazy(TestCaseAssert::assertNotLazyLoad)
                    .assertNestedChain(TestCaseAssert::assertNotNestedChain)
                    .create();
        }
    }

    public static class ParseScriptTypeWithFullContents extends ParseScriptTest {

        @Before
        public void setUp() throws IOException {
            this.result = target.load(testFileScriptWithFullContents);
        }

        @Override
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert::assertEqualsNoRelationFile)
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(1))
                    .assertTestCase(0, this.getTestCaseAssert())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
                    .create();
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(testFileScriptWithFullContents))
                    .assertStep(TestCaseAssert.assertEqualsStepCount(9))
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(DATA_SET_CSV))
                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                    .assertOverrideDataSource(TestCaseAssert::assertEqualsNoOverrideDataSource)
                    .assertLazy(TestCaseAssert::assertNotLazyLoad)
                    .assertNestedChain(TestCaseAssert::assertNotNestedChain)
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert::assertEqualsNoScript)
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert::assertEqualsNoScript)
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert.assertEqualsDataSet(DATA_SET_CSV))
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(3))
                    .assertTestCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_CSV))
                            .create())
                    .assertTestCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertTestCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(3))
                    .assertTestCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_CSV))
                            .create())
                    .assertTestCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertTestCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertChainCount(SuiteAssert.assertEqualsChainCount(2))
                    .assertChain(SuiteAssert.assertEqualsChain(0, 1))
                    .assertChain(SuiteAssert.assertEqualsChain(1, 2))
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(3))
                    .assertTestCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_CSV))
                            .create())
                    .assertTestCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .assertNestedChain(TestCaseAssert::assertNestedChain)
                            .create())
                    .assertTestCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertChainCount(SuiteAssert.assertEqualsChainCount(2))
                    .assertChain(SuiteAssert.assertEqualsChain(0, 1))
                    .assertChain(SuiteAssert.assertEqualsChain(1, 2))
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(3))
                    .assertTestCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(new TestDataSet(new Csv(), Map.of("path", "${path}/override.csv"), new File(baseDir))))
                            .create())
                    .assertTestCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertTestCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert::assertEqualsNoDataSource)
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(3))
                    .assertTestCase(0, TestCaseAssert.of()
                            .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("${script1}.json"))
                            .assertSkip(TestCaseAssert.assertEqualsSkip("${skip1}"))
                            .assertStep(TestCaseAssert::assertEqualsNoStep)
                            .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_OVERRIDE_LAZY))
                            .assertLazy(TestCaseAssert::assertLazyLoad)
                            .assertNestedChain(TestCaseAssert::assertNotNestedChain)
                            .create())
                    .assertTestCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertTestCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .create())
                    .assertChainCount(SuiteAssert::assertEqualsNoScriptChain)
                    .assertDataSource(SuiteAssert.assertEqualsDataSet(new TestDataSet(new Manual()
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
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(3))
                    .assertTestCase(0, TestCaseAssert.of()
                            .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("${script1}.json"))
                            .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                            .assertStep(TestCaseAssert::assertEqualsNoStep)
                            .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                            .assertOverrideDataSource(TestCaseAssert::assertEqualsNoOverrideDataSource)
                            .assertLazy(TestCaseAssert::assertLazyLoad)
                            .assertNestedChain(TestCaseAssert::assertNotNestedChain)
                            .create())
                    .assertTestCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .create())
                    .assertTestCase(2, TestCaseAssert.of()
                            .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("${script3}.json"))
                            .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                            .assertStep(TestCaseAssert::assertEqualsNoStep)
                            .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(new TestDataSet(new Csv(), Map.of("path", "${dataSource3}.csv"), null)))
                            .assertLazy(TestCaseAssert::assertLazyLoad)
                            .assertNestedChain(TestCaseAssert::assertNotNestedChain)
                            .create())
                    .assertChainCount(SuiteAssert.assertEqualsChainCount(2))
                    .assertChain(SuiteAssert.assertEqualsChain(0, 1))
                    .assertChain(SuiteAssert.assertEqualsChain(1, 2))
                    .assertDataSource(SuiteAssert.assertEqualsDataSet(new TestDataSet(new Manual()
                            , Map.of("script1", "scriptWithDataSource", "script3", "scriptWithNoContents", "dataSource3", "csv/override")
                            , new File(baseDir))))
                    .create();
        }
    }
}