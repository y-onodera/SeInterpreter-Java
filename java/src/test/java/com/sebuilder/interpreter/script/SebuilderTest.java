package com.sebuilder.interpreter.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.datasource.Manual;
import com.sebuilder.interpreter.datasource.None;
import com.sebuilder.interpreter.pointcut.*;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import com.sebuilder.interpreter.step.getter.ElementEnable;
import com.sebuilder.interpreter.step.getter.ElementPresent;
import com.sebuilder.interpreter.step.type.Get;
import com.sebuilder.interpreter.step.type.SetElementSelected;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;
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

        protected InputData sharInput = new InputData();

        @Test
        public void parseResultContents() {
            this.getTestCaseAssert().run(this.result.builder().setShareInput(this.sharInput).build());
        }

        @Test
        public void parseResultReversible() {
            final TestCase reverse = target.load(toStringConverter.toString(this.result), Strings.isNullOrEmpty(this.result.path()) ? null : new File(this.result.path()));
            this.getTestCaseAssert().run(reverse.builder().setShareInput(this.sharInput).build());
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
                    .build();
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
                    .build();
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
                    .build();
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
                    .build();
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
                    .build();
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
                    .build();
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
                    .build();
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
                            .build())
                    .assertChainCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .build())
                    .assertChainCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .build())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .build();
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
                                    .build())
                            .assertChainCase(1, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                                    .builder()
                                    .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                                    .build())
                            .build())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .build();
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
                                    .build())
                            .assertChainCase(2, new ParseScriptTypeWithSteps().getTestCaseAssert()
                                    .builder()
                                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("scriptWithSteps.json(1)", testFileScriptWithSteps))
                                    .build())
                            .build())
                    .build();
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
                                            .build())
                                    .build())
                            .build())
                    .build();
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
                            .build())
                    .assertChainCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .build())
                    .assertChainCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .build())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .build();
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
                            .build())
                    .assertChainCase(1, new ParseScriptTypeWithSteps().getTestCaseAssert()
                            .builder()
                            .assertSkip(TestCaseAssert.assertEqualsSkip("true"))
                            .build())
                    .assertChainCase(2, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                            .build())
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(new DataSourceLoader(new Manual()
                            , Map.of("script1", "scriptWithNoContents", "skip1", "false", "dataSource1", "override")
                            , new File(baseDir))))
                    .build();
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
                                    .build())
                            .assertChainCase(1, TestCaseAssert.of()
                                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute("${script3}.json"))
                                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                                    .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(new DataSourceLoader(new Csv(), Map.of("path", "${dataSource3}.csv"), null)))
                                    .assertLazy(TestCaseAssert::assertLazyLoad)
                                    .build())
                            .build())
                    .assertDataSource(TestCaseAssert.assertEqualsDataSet(new DataSourceLoader(new Manual()
                            , Map.of("script1", "scriptWithDataSource", "script3", "scriptWithNoContents", "dataSource3", "csv/override")
                            , new File(baseDir))))
                    .build();
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
                    .build();
        }
    }

    public static class ParseSuiteWithTestRunFilter extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithTestRunFilter.json");

        @Before
        public void setUp() {
            this.result = target.load(this.testFile);
            this.sharInput = this.sharInput.add("excludeImport", "pointcut/locatorFilter.json");
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .materialized(true)
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
                                    .build())
                            .assertChainCase(1, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                                    .builder()
                                    .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                                    .assertIncludeTestRun(it -> assertEquals(
                                            new TypeFilter("SetElementText")
                                                    .or(new TypeFilter("SelectElementValue"))
                                                    .or(new TypeFilter("SetElementSelected"))
                                                    .and(new VerifyFilter(false, new ElementEnable().toVerify(), new HashMap<>()))
                                                    .and(new SkipFilter(false))
                                                    .and(new VerifyFilter(false, new ElementPresent().toVerify(), new HashMap<>() {{
                                                        this.put("negated", "false");
                                                    }}))
                                            , it))
                                    .assertExcludeTestRun(it -> assertEquals(
                                            new LocatorFilter("locator", new Locator("id", "id1"))
                                                    .or(new LocatorFilter("locator", new Locator("id", "id2")))
                                                    .or(new LocatorFilter("locator", new Locator("id", "id3")))
                                            , it))
                                    .build())
                            .build())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .build();
        }
    }

    public static class ParseSuiteWithAspect extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithAspect.json");

        @Before
        public void setUp() {
            this.result = target.load(this.testFile);
        }

        @Override
        public TestCaseAssert getTestCaseAssert() {
            return TestCaseAssert.of()
                    .materialized(true)
                    .assertFileAttribute(TestCaseAssert.assertEqualsFileAttribute(this.testFile))
                    .assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(1))
                    .assertChainCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertAspect(it -> Assert.assertEquals(new Aspect().builder()
                                    .add(new ExtraStepExecutor.Builder()
                                            .setPointcut(new TypeFilter("SetElementText")
                                                    .or(new TypeFilter("SelectElementValue"))
                                                    .or(new TypeFilter("SetElementSelected"))
                                                    .and(new LocatorFilter("locator", new Locator("id", "id1"))
                                                            .or(new LocatorFilter("locator", new Locator("id", "id2")))
                                                            .or(new LocatorFilter("locator", new Locator("id", "id3"))))
                                                    .and(new SkipFilter(false))
                                            ).addAfter(new TestCaseBuilder()
                                                    .addStep(new SetElementText().toStep().put("text", "after step").build())
                                                    .addStep(new Get().toStep().build())
                                                    .build()
                                            ).addBefore(new TestCaseBuilder()
                                                    .addStep(new Get().toStep().build())
                                                    .addStep(new SetElementText().toStep().put("text", "before step").build())
                                                    .build()
                                            ).addFailure(new TestCaseBuilder()
                                                    .addStep(new SetElementSelected().toStep().build())
                                                    .addStep(new SetElementText().toStep().put("text", "failure step").build())
                                                    .build()
                                            )
                                    )
                                    .build(), it))
                            .assertChainCaseCounts(2)
                            .assertChainCase(0, new ParseScriptTypeWithSteps().getTestCaseAssert()
                                    .builder()
                                    .assertAspect(it -> Assert.assertEquals(new Aspect().builder()
                                            .add(new ExtraStepExecutor.Builder()
                                                    .setPointcut(new SkipFilter(false)
                                                            .and(new TypeFilter("SetElementText")
                                                                    .or(new TypeFilter("SelectElementValue"))
                                                                    .or(new TypeFilter("SetElementSelected"))
                                                            )
                                                            .and(new LocatorFilter("locator", new Locator("id", "id1"))
                                                                    .or(new LocatorFilter("locator", new Locator("id", "id2")))
                                                                    .or(new LocatorFilter("locator", new Locator("id", "id3"))))
                                                    ).addAfter(new TestCaseBuilder()
                                                            .addStep(new SetElementText().toStep()
                                                                    .put("text", "after step")
                                                                    .put(Step.KEY_NAME_SKIP, "false")
                                                                    .build())
                                                            .addStep(new Get().toStep()
                                                                    .put(Step.KEY_NAME_SKIP, "false")
                                                                    .build())
                                                            .build()
                                                    ).addBefore(new TestCaseBuilder()
                                                            .addStep(new Get().toStep()
                                                                    .put(Step.KEY_NAME_SKIP, "false")
                                                                    .build())
                                                            .addStep(new SetElementText().toStep()
                                                                    .put("text", "before step")
                                                                    .put(Step.KEY_NAME_SKIP, "false")
                                                                    .build())
                                                            .build()
                                                    ).addFailure(new TestCaseBuilder()
                                                            .addStep(new SetElementSelected().toStep()
                                                                    .put(Step.KEY_NAME_SKIP, "false")
                                                                    .build())
                                                            .addStep(new SetElementText().toStep()
                                                                    .put("text", "failure step")
                                                                    .put(Step.KEY_NAME_SKIP, "false")
                                                                    .build())
                                                            .build()
                                                    )
                                            )
                                            .build(), it))
                                    .build()
                            )
                            .assertChainCase(1, new ParseScriptTypeWithDataSource().getTestCaseAssert()
                                    .builder()
                                    .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(DATA_SET_NONE))
                                    .build())
                            .build())
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .build();
        }
    }
}