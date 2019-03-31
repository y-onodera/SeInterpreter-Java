package com.sebuilder.interpreter.factory;

import com.sebuilder.interpreter.DataSet;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.SuiteAssert;
import com.sebuilder.interpreter.TestCaseAssert;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.datasource.Manual;
import com.sebuilder.interpreter.datasource.None;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RunWith(Enclosed.class)
public class TestCaseFactoryTest {

    private static String baseDir = TestCaseFactoryTest.class.getResource(".").getPath();
    private static TestCaseFactory target = new TestCaseFactory();
    private static ScriptConverter converter = new ScriptConverter();

    private static final DataSet DATA_SET_NONE = new DataSet(new None(), Map.of(), new File(baseDir));
    private static final DataSet DATA_SET_CSV = new DataSet(new Csv(), Map.of("path", "test.csv"), new File(baseDir));
    private static final DataSet DATA_SET_OVERRIDE_CSV = new DataSet(new Csv(), Map.of("path", "override.csv"), new File(baseDir));
    private static final DataSet DATA_SET_OVERRIDE_LAZY = new DataSet(new Csv(), Map.of("path", "${dataSource1}.csv"), null);

    private static final File testFileNoType = new File(baseDir, "noType.json");

    private static final File testFileScriptWithNoContents = new File(baseDir, "scriptWithNoContents.json");
    private static final File testFileScriptWithDataSource = new File(baseDir, "scriptWithDataSource.json");
    private static final File testFileScriptWithSteps = new File(baseDir, "scriptWithSteps.json");
    private static final File testFileScriptWithFullContents = new File(baseDir, "scriptWithFullContents.json");

    public static abstract class ParseResultTest {
        protected Suite result;

        @Test
        public void parseResultContents() {
            this.getSuiteAssert().run(result);
        }

        @Test
        public void parseResultReversible() throws IOException, JSONException {
            Suite reverse = target.parse(converter.toString(this.result), com.google.common.base.Strings.isNullOrEmpty(this.result.getPath()) ? null : new File(this.result.getPath()));
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
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(testFileNoType);
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
                    .assertLazy(TestCaseAssert::assertEqualsNotLazyLoad)
                    .create();
        }
    }

    public static class ParseScriptNoContents extends ParseScriptTest {

        @Before
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(testFileScriptWithNoContents);
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
                    .assertLazy(TestCaseAssert::assertEqualsNotLazyLoad)
                    .create();
        }

    }

    public static class ParseScriptTypeWithDataSource extends ParseScriptTest {

        @Before
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(testFileScriptWithDataSource);
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
                    .assertLazy(TestCaseAssert::assertEqualsNotLazyLoad)
                    .create();
        }
    }

    public static class ParseScriptTypeWithSteps extends ParseScriptTest {

        @Before
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(testFileScriptWithSteps);
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
                    .assertStep(TestCaseAssert.assertEqualsStepCount(9))
                    .assertDataSource(TestCaseAssert::assertEqualsNoDataSource)
                    .assertSkip(TestCaseAssert.assertEqualsSkip("false"))
                    .assertOverrideDataSource(TestCaseAssert::assertEqualsNoOverrideDataSource)
                    .assertLazy(TestCaseAssert::assertEqualsNotLazyLoad)
                    .create();
        }
    }

    public static class ParseScriptTypeWithFullContents extends ParseScriptTest {

        @Before
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(testFileScriptWithFullContents);
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
                    .assertLazy(TestCaseAssert::assertEqualsNotLazyLoad)
                    .create();
        }
    }

    public static class ParseSuiteNoContents extends ParseResultTest {

        private final File testFile = new File(baseDir, "simpleSuite.json");

        @Before
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(this.testFile);
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
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(this.testFile);
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
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(this.testFile);
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
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(this.testFile);
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

    public static class ParseSuiteWithScriptRuntimeDataSource extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithScriptRuntimeDataSource.json");

        @Before
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(this.testFile);
        }

        @Override
        public SuiteAssert getSuiteAssert() {
            return SuiteAssert.of()
                    .assertFileAttribute(SuiteAssert.assertEqualsFileAttribute(testFile))
                    .assertTestCaseCount(SuiteAssert.assertEqualsTestCaseCount(3))
                    .assertTestCase(0, new ParseScriptNoContents().getTestCaseAssert()
                            .builder()
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(new DataSet(new Csv(), Map.of("path", "${path}/override.csv"), new File(baseDir))))
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
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(this.testFile);
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
                            .assertLazy(TestCaseAssert::assertEqualsLazyLoad)
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
                    .assertDataSource(SuiteAssert.assertEqualsDataSet(new DataSet(new Manual()
                            , Map.of("script1", "scriptWithNoContents", "skip1", "false", "dataSource1", "override")
                            , new File(baseDir))))
                    .create();
        }
    }

    public static class ParseSuiteWithLazyScriptChain extends ParseResultTest {

        private final File testFile = new File(baseDir, "suiteWithLazyScriptChain.json");

        @Before
        public void setUp() throws IOException, JSONException {
            this.result = target.parse(this.testFile);
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
                            .assertLazy(TestCaseAssert::assertEqualsLazyLoad)
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
                            .assertOverrideDataSource(TestCaseAssert.assertEqualsOverrideDataSst(new DataSet(new Csv(), Map.of("path", "${dataSource3}.csv"), null)))
                            .assertLazy(TestCaseAssert::assertEqualsLazyLoad)
                            .create())
                    .assertChainCount(SuiteAssert.assertEqualsChainCount(2))
                    .assertChain(SuiteAssert.assertEqualsChain(0, 1))
                    .assertChain(SuiteAssert.assertEqualsChain(1, 2))
                    .assertDataSource(SuiteAssert.assertEqualsDataSet(new DataSet(new Manual()
                            , Map.of("script1", "scriptWithDataSource", "script3", "scriptWithNoContents", "dataSource3", "csv/override")
                            , new File(baseDir))))
                    .create();
        }
    }
}