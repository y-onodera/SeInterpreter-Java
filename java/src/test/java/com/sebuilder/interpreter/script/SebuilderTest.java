package com.sebuilder.interpreter.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.datasource.Manual;
import com.sebuilder.interpreter.datasource.None;
import com.sebuilder.interpreter.pointcut.LocatorFilter;
import com.sebuilder.interpreter.pointcut.SkipFilter;
import com.sebuilder.interpreter.pointcut.StringParamFilter;
import com.sebuilder.interpreter.pointcut.TypeFilter;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
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

    public static abstract class ParseResultTest {
        protected TestCase result;

        @Test
        public void parseResultContents() {
            this.getTestCaseAssert().run(this.result);
        }

        @Test
        public void parseResultReversible() {
            final TestCase reverse = target.load(toStringConverter.toString(this.result), Strings.isNullOrEmpty(this.result.path()) ? null : new File(this.result.path()));
            this.getTestCaseAssert().run(reverse);
        }

        public abstract TestCaseAssert getTestCaseAssert();

    }

    public static class ParseNoType extends ParseResultTest {

        @Before
        public void setUp() {
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
        public void setUp() {
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
        public void setUp() {
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
        public void setUp() {
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
        public void setUp() {
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
                    .create();
        }
    }

    public static class ParseSuiteWithDataSource extends ParseResultTest {


        private final File testFile = new File(baseDir, "suiteWithDataSource.json");

        @Before
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(DATA_SET_CSV))
                    .create();
        }
    }

    public static class ParseSuiteWithScripts extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithScripts.json");

        @Before
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
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
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(2))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert())
                    .assertChainCase(1, new ParseSuiteWithScripts().getTestCaseAssert())
                    .create();
        }
    }

    public static class ParseSuiteWithTestRunFilter extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithTestRunFilter.json");

        @Before
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertIncludeTestRun(it -> assertEquals(
                                    new SkipFilter(false)
                                            .and(new TypeFilter("SetElementText"))
                                            .and(new LocatorFilter("locator", new Locator("id", "id1")))
                                    , it))
                            .assertExcludeTestRun(it -> assertEquals(
                                    new TypeFilter("SetElementText")
                                            .and(new LocatorFilter("locator", new Locator("id", "id1")))
                                    , it))
                            .assertChainCaseCounts(2)
                            .assertChainCase(0, new ParseScriptTypeWithSteps().getTestCaseAssert()
                                    .builder()
                                    .assertIncludeTestRun(it -> assertEquals(
                                            new StringParamFilter("text", "false", "contains")
                                                    .and(new TypeFilter("SetElementText")
                                                            .or(new TypeFilter("SelectElementValue"))
                                                            .or(new TypeFilter("SetElementSelected")))
                                                    .and(new LocatorFilter("locator", new Locator("id", "id1"))
                                                            .or(new LocatorFilter("locator", new Locator("id", "id2")))
                                                            .or(new LocatorFilter("locator", new Locator("id", "id3"))))
                                            , it))
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

}