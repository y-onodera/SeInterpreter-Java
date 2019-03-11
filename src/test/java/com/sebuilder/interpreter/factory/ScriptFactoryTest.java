package com.sebuilder.interpreter.factory;

import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.TestData;
import com.sebuilder.interpreter.TestRunBuilder;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.datasource.None;
import org.json.JSONException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class ScriptFactoryTest {

    private static String baseDir = ScriptFactoryTest.class.getResource(".").getPath();
    private static ScriptFactory target = new ScriptFactory();

    public static class ScriptTest {

        @Test
        public void parseScript() throws IOException, JSONException {
            final String testFile = "simpleScript.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertSuiteAttribute(result);
            Script actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertSimpleScript(testFile, actual);

            result = target.parse(result.toJSON(), null);
            assertSuiteAttribute(result);
            actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertSimpleScript(testFile, actual);
        }

        @Test
        public void parseNoTypeFileAsScript() throws IOException, JSONException {
            final String testFile = "simpleScriptNoType.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertSuiteAttribute(result);
            Script actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertSimpleScript(testFile, actual);

            result = target.parse(result.toJSON(), null);
            assertSuiteAttribute(result);
            actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertSimpleScript(testFile, actual);
        }

        @Test
        public void parseScriptWithDataSource() throws IOException, JSONException {
            final String testFile = "scriptWithDataSource.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertSuiteAttribute(result);
            Script actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertScriptWithDataSource(testFile, actual);

            result = target.parse(result.toJSON(), null);
            assertSuiteAttribute(result);
            actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertScriptWithDataSource(testFile, actual);
        }

        @Test
        public void parseScriptWithSteps() throws IOException, JSONException {
            final String testFile = "scriptWithSteps.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertSuiteAttribute(result);
            Script actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertScriptWithSteps(testFile, actual);

            result = target.parse(result.toJSON(), null);
            assertSuiteAttribute(result);
            actual = result.get(testFile);
            assertSame(actual, result.get(0));
            assertScriptWithSteps(testFile, actual);
        }

        private void assertSuiteAttribute(Suite result) {
            assertEquals(Suite.DEFAULT_NAME, result.getName());
            assertNull(result.getPath());
            assertEquals(1, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertNull(result.getDataSource());
            assertEquals(0, result.getDataSourceConfig().size());
            assertTrue(result.isShareState());
        }

    }

    public static class SuiteTest {

        @Test
        public void parseSuite() throws IOException, JSONException {
            final String testFile = "simpleSuite.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertEquals(0, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertNoScript(result);
            assertNoDataSource(result);
            assertTrue(result.isShareState());

            result = target.parse(result.toJSON(), new File(result.getPath()));
            assertEquals(0, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertNoScript(result);
            assertNoDataSource(result);
            assertTrue(result.isShareState());
        }

        @Test
        public void parseSuiteWithDataSource() throws IOException, JSONException {
            final String testFile = "suiteWithDataSource.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertEquals(0, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertNoScript(result);
            assertTrue(result.getDataSource() instanceof Csv);
            assertEquals(1, result.getDataSourceConfig().size());
            assertEquals("test.csv", result.getDataSourceConfig().get("path"));
            assertTrue(result.isShareState());

            result = target.parse(result.toJSON(), new File(result.getPath()));
            assertEquals(0, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertNoScript(result);
            assertTrue(result.getDataSource() instanceof Csv);
            assertEquals(1, result.getDataSourceConfig().size());
            assertEquals("test.csv", result.getDataSourceConfig().get("path"));
            assertTrue(result.isShareState());
        }

        @Test
        public void parseSuiteWithScripts() throws IOException, JSONException {
            final String testFile = "suiteWithScripts.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertEquals(3, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertSimpleScript("simpleScript.json", result.get(0));
            assertEquals("false", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("override.csv", result.get(0).overrideDataSourceConfig().get("path"));
            List<TestData> loadData = result.get(0).loadData(new TestData());
            assertEquals(2, loadData.size());
            assertEquals("a", loadData.get(0).get("column1"));
            assertEquals("b", loadData.get(0).get("column2"));
            assertEquals("1", loadData.get(1).get("column1"));
            assertEquals("2", loadData.get(1).get("column2"));
            assertScriptWithSteps("scriptWithSteps.json", result.get(1));
            assertEquals("true", result.get(1).skip());
            assertNull(result.get(1).overrideDataSource());
            assertEquals(0, result.get(1).overrideDataSourceConfig().size());
            assertScriptWithDataSource("scriptWithDataSource.json", result.get(2));
            assertEquals("false", result.get(2).skip());
            assertTrue(result.get(2).overrideDataSource() instanceof None);
            assertEquals(0, result.get(2).overrideDataSourceConfig().size());
            assertNoDataSource(result);
            assertTrue(result.isShareState());

            result = target.parse(result.toJSON(), new File(result.getPath()));
            assertEquals(3, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertSimpleScript("simpleScript.json", result.get(0));
            assertEquals("false", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("override.csv", result.get(0).overrideDataSourceConfig().get("path"));
            result.get(0).loadData(new TestData());
            assertEquals(2, loadData.size());
            assertEquals("a", loadData.get(0).get("column1"));
            assertEquals("b", loadData.get(0).get("column2"));
            assertEquals("1", loadData.get(1).get("column1"));
            assertEquals("2", loadData.get(1).get("column2"));
            assertScriptWithSteps("scriptWithSteps.json", result.get(1));
            assertEquals("true", result.get(1).skip());
            assertNull(result.get(1).overrideDataSource());
            assertEquals(0, result.get(1).overrideDataSourceConfig().size());
            assertScriptWithDataSource("scriptWithDataSource.json", result.get(2));
            assertEquals("false", result.get(2).skip());
            assertTrue(result.get(2).overrideDataSource() instanceof None);
            assertEquals(0, result.get(2).overrideDataSourceConfig().size());
            assertNoDataSource(result);
            assertTrue(result.isShareState());
        }

        @Test
        public void parseSuiteWithLazyScript() throws IOException, JSONException {
            final String testFile = "suiteWithLazyScript.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertEquals(3, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertEquals("${script1}.json", result.get(0).name());
            assertEquals("scriptPath", result.get(0).path());
            assertNull(result.get(0).relativePath());
            assertEquals("${skip1}", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("${dataSource1}.csv", result.get(0).overrideDataSourceConfig().get("path"));
            assertScriptWithSteps("scriptWithSteps.json", result.get(1));
            assertEquals("true", result.get(1).skip());
            assertNull(result.get(1).overrideDataSource());
            assertEquals(0, result.get(1).overrideDataSourceConfig().size());
            assertScriptWithDataSource("scriptWithDataSource.json", result.get(2));
            assertEquals("false", result.get(2).skip());
            assertTrue(result.get(2).overrideDataSource() instanceof None);
            assertEquals(0, result.get(2).overrideDataSourceConfig().size());
            assertTrue(result.isShareState());
            List<TestRunBuilder> lazyFetch = result.getTestRuns();
            List<TestData> loadData = lazyFetch.get(0).loadData();
            assertEquals(2, loadData.size());
            assertEquals("a", loadData.get(0).get("column1"));
            assertEquals("b", loadData.get(0).get("column2"));
            assertEquals("1", loadData.get(1).get("column1"));
            assertEquals("2", loadData.get(1).get("column2"));

            result = target.parse(result.toJSON(), new File(result.getPath()));
            assertEquals(3, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertEquals("${script1}.json", result.get(0).name());
            assertEquals("scriptPath", result.get(0).path());
            assertNull(result.get(0).relativePath());
            assertEquals("${skip1}", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("${dataSource1}.csv", result.get(0).overrideDataSourceConfig().get("path"));
            assertScriptWithSteps("scriptWithSteps.json", result.get(1));
            assertEquals("true", result.get(1).skip());
            assertNull(result.get(1).overrideDataSource());
            assertEquals(0, result.get(1).overrideDataSourceConfig().size());
            assertScriptWithDataSource("scriptWithDataSource.json", result.get(2));
            assertEquals("false", result.get(2).skip());
            assertTrue(result.get(2).overrideDataSource() instanceof None);
            assertEquals(0, result.get(2).overrideDataSourceConfig().size());
            assertTrue(result.isShareState());
            lazyFetch = result.getTestRuns();
            loadData = lazyFetch.get(0).loadData();
            assertEquals(2, loadData.size());
            assertEquals("a", loadData.get(0).get("column1"));
            assertEquals("b", loadData.get(0).get("column2"));
            assertEquals("1", loadData.get(1).get("column1"));
            assertEquals("2", loadData.get(1).get("column2"));
        }

        @Test
        public void parseSuiteWithLazyScriptChain() throws IOException, JSONException {
            final String testFile = "suiteWithLazyScriptChain.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertEquals(3, result.scriptSize());
            assertEquals(2, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertEquals("${script1}.json", result.get(0).name());
            assertEquals("scriptPath", result.get(0).path());
            assertNull(result.get(0).relativePath());
            assertEquals("false", result.get(0).skip());
            assertNull(result.get(0).overrideDataSource());
            assertEquals(0, result.get(0).overrideDataSourceConfig().size());
            assertScriptWithSteps("scriptWithSteps.json", result.get(1));
            assertSame(result.get(1), result.getScriptChains().get(result.get(0)));
            assertEquals("true", result.get(1).skip());
            assertNull(result.get(1).overrideDataSource());
            assertEquals(0, result.get(1).overrideDataSourceConfig().size());
            assertEquals("${script3}.json", result.get(2).name());
            assertEquals("scriptPath", result.get(2).path());
            assertSame(result.get(2), result.getScriptChains().get(result.get(1)));
            assertEquals("false", result.get(2).skip());
            assertTrue(result.get(2).overrideDataSource() instanceof Csv);
            assertEquals("${dataSource3}.csv", result.get(2).overrideDataSourceConfig().get("path"));
            assertTrue(result.isShareState());
            List<TestRunBuilder> lazyFetch = result.getTestRuns();
            List<TestData> loadData = lazyFetch.get(0).loadData();
            assertEquals(2, loadData.size());
            assertEquals("A", loadData.get(0).get("column1"));
            assertEquals("B", loadData.get(0).get("column2"));
            assertEquals("2", loadData.get(1).get("column1"));
            assertEquals("1", loadData.get(1).get("column2"));

            result = target.parse(result.toJSON(), new File(result.getPath()));
            assertEquals(3, result.scriptSize());
            assertEquals(2, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertEquals("${script1}.json", result.get(0).name());
            assertEquals("scriptPath", result.get(0).path());
            assertNull(result.get(0).relativePath());
            assertEquals("false", result.get(0).skip());
            assertNull(result.get(0).overrideDataSource());
            assertEquals(0, result.get(0).overrideDataSourceConfig().size());
            assertScriptWithSteps("scriptWithSteps.json", result.get(1));
            assertSame(result.get(1), result.getScriptChains().get(result.get(0)));
            assertEquals("true", result.get(1).skip());
            assertNull(result.get(1).overrideDataSource());
            assertEquals(0, result.get(1).overrideDataSourceConfig().size());
            assertEquals("${script3}.json", result.get(2).name());
            assertEquals("scriptPath", result.get(2).path());
            assertSame(result.get(2), result.getScriptChains().get(result.get(1)));
            assertEquals("false", result.get(2).skip());
            assertTrue(result.get(2).overrideDataSource() instanceof Csv);
            assertEquals("${dataSource3}.csv", result.get(2).overrideDataSourceConfig().get("path"));
            assertTrue(result.isShareState());
            lazyFetch = result.getTestRuns();
            loadData = lazyFetch.get(0).loadData();
            assertEquals(2, loadData.size());
            assertEquals("A", loadData.get(0).get("column1"));
            assertEquals("B", loadData.get(0).get("column2"));
            assertEquals("2", loadData.get(1).get("column1"));
            assertEquals("1", loadData.get(1).get("column2"));
        }

        @Test
        public void parseSuiteWithScriptChain() throws IOException, JSONException {
            final String testFile = "suiteWithScriptChain.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertEquals(3, result.scriptSize());
            assertEquals(2, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertSimpleScript("simpleScript.json", result.get(0));
            assertEquals("false", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("override.csv", result.get(0).overrideDataSourceConfig().get("path"));
            Script script2 = result.getScriptChains().get(result.get(0));
            assertSame(result.get(1), script2);
            assertScriptWithSteps("scriptWithSteps.json", script2);
            assertEquals("true", script2.skip());
            assertNull(script2.overrideDataSource());
            assertEquals(0, script2.overrideDataSourceConfig().size());
            Script script3 = result.getScriptChains().get(script2);
            assertSame(result.get(2), script3);
            assertScriptWithDataSource("scriptWithDataSource.json", script3);
            assertEquals("false", script3.skip());
            assertTrue(script3.overrideDataSource() instanceof None);
            assertEquals(0, script3.overrideDataSourceConfig().size());
            assertNoDataSource(result);
            assertTrue(result.isShareState());

            result = target.parse(result.toJSON(), new File(result.getPath()));
            assertEquals(3, result.scriptSize());
            assertEquals(2, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertSimpleScript("simpleScript.json", result.get(0));
            assertEquals("false", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("override.csv", result.get(0).overrideDataSourceConfig().get("path"));
            script2 = result.getScriptChains().get(result.get(0));
            assertSame(result.get(1), script2);
            assertScriptWithSteps("scriptWithSteps.json", script2);
            assertEquals("true", script2.skip());
            assertNull(script2.overrideDataSource());
            assertEquals(0, script2.overrideDataSourceConfig().size());
            script3 = result.getScriptChains().get(script2);
            assertSame(result.get(2), script3);
            assertScriptWithDataSource("scriptWithDataSource.json", script3);
            assertEquals("false", script3.skip());
            assertTrue(script3.overrideDataSource() instanceof None);
            assertEquals(0, script3.overrideDataSourceConfig().size());
            assertNoDataSource(result);
            assertTrue(result.isShareState());
        }

        @Test
        public void parseSuiteWithScriptRuntimeDataSource() throws IOException, JSONException {
            final String testFile = "suiteWithScriptRuntimeDataSource.json";
            final File testSource = new File(baseDir, testFile);

            Suite result = target.parse(testSource);
            assertEquals(3, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertSimpleScript("simpleScript.json", result.get(0));
            assertEquals("false", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("${path}/override.csv", result.get(0).overrideDataSourceConfig().get("path"));
            List<TestData> loadData = result.get(0).loadData(new TestData().add("path", "csv"));
            assertEquals(2, loadData.size());
            assertEquals("1", loadData.get(0).get("column1"));
            assertEquals("2", loadData.get(0).get("column2"));
            assertEquals("a", loadData.get(1).get("column1"));
            assertEquals("b", loadData.get(1).get("column2"));
            Script script2 = result.get(1);
            assertScriptWithSteps("scriptWithSteps.json", script2);
            assertEquals("true", script2.skip());
            assertNull(script2.overrideDataSource());
            assertEquals(0, script2.overrideDataSourceConfig().size());
            Script script3 = result.get(2);
            assertScriptWithDataSource("scriptWithDataSource.json", script3);
            assertEquals("false", script3.skip());
            assertTrue(script3.overrideDataSource() instanceof None);
            assertEquals(0, script3.overrideDataSourceConfig().size());
            assertNoDataSource(result);
            assertTrue(result.isShareState());

            result = target.parse(result.toJSON(), new File(result.getPath()));
            assertEquals(3, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
            assertFileAttribute(testFile, testSource, result);
            assertSimpleScript("simpleScript.json", result.get(0));
            assertEquals("false", result.get(0).skip());
            assertTrue(result.get(0).overrideDataSource() instanceof Csv);
            assertEquals("${path}/override.csv", result.get(0).overrideDataSourceConfig().get("path"));
            loadData = result.get(0).loadData(new TestData().add("path", "csv"));
            assertEquals(2, loadData.size());
            assertEquals("1", loadData.get(0).get("column1"));
            assertEquals("2", loadData.get(0).get("column2"));
            assertEquals("a", loadData.get(1).get("column1"));
            assertEquals("b", loadData.get(1).get("column2"));
            script2 = result.get(1);
            assertScriptWithSteps("scriptWithSteps.json", script2);
            assertEquals("true", script2.skip());
            assertNull(script2.overrideDataSource());
            assertEquals(0, script2.overrideDataSourceConfig().size());
            script3 = result.get(2);
            assertScriptWithDataSource("scriptWithDataSource.json", script3);
            assertEquals("false", script3.skip());
            assertTrue(script3.overrideDataSource() instanceof None);
            assertEquals(0, script3.overrideDataSourceConfig().size());
            assertNoDataSource(result);
            assertTrue(result.isShareState());
        }

        private void assertFileAttribute(String testFile, File testSource, Suite result) {
            assertEquals(testFile, result.getName());
            assertEquals(testSource.getAbsolutePath(), result.getPath());
            assertEquals(testSource.getParentFile().getAbsoluteFile(), result.getRelativePath());
        }

        private void assertNoScript(Suite result) {
            assertEquals(0, result.scriptSize());
            assertEquals(0, result.getScriptChains().size());
        }

        private void assertNoDataSource(Suite result) {
            assertNull(result.getDataSource());
            assertEquals(0, result.getDataSourceConfig().size());
        }

    }

    private static void assertSimpleScript(String testFile, Script actual) {
        assertFileAttribute(testFile, actual);
        assertNoDataSource(actual);
        assertNoSteps(actual);
    }

    private static void assertScriptWithSteps(String testFile, Script actual) {
        assertFileAttribute(testFile, actual);
        assertNoDataSource(actual);
        assertEquals(9, actual.steps().size());
    }

    private static void assertScriptWithDataSource(String testFile, Script actual) {
        assertFileAttribute(testFile, actual);
        assertTrue(actual.dataSource() instanceof Csv);
        assertEquals(1, actual.dataSourceConfig().size());
        assertEquals("test.csv", actual.dataSourceConfig().get("path"));
        assertNoSteps(actual);
    }

    private static void assertFileAttribute(String testFile, Script actual) {
        assertEquals(testFile, actual.name());
        assertEquals(new File(baseDir), actual.relativePath());
    }

    private static void assertNoDataSource(Script actual) {
        assertNull(actual.dataSource());
        assertEquals(0, actual.dataSourceConfig().size());
    }

    private static void assertNoSteps(Script actual) {
        assertEquals(0, actual.steps().size());
    }
}