package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.application.TestRunListenerImpl;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.step.getter.ElementAttribute;
import com.sebuilder.interpreter.step.type.ClickElement;
import com.sebuilder.interpreter.step.type.DoubleClickElement;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.apache.logging.log4j.Logger;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class TestRunTest {

    public static abstract class AbstractTestRunTest {
        @Rule
        public MockitoRule rule = MockitoJUnit.rule();
        @Rule
        public ExpectedException expectedException = ExpectedException.none();
        @Mock
        RemoteWebDriver driver;
        @Mock
        Logger log;
        @Mock
        TestRunListener listener;
        @Mock
        Step step;
        TestData initialVars;
        TestCase testCase;
        Scenario scenario;
        TestRun target;

        @Before
        public void setUpStub() {
            Context.getInstance().setBrowser("Chrome");
            Context.getInstance().setDataSourceDirectory("datasource");
            Mockito.doReturn(new File(".", "result")).when(this.listener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.listener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.listener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.listener).getDownloadDirectory();
            this.testCase = new TestCaseBuilder().build();
            this.scenario = new Scenario(this.testCase);
            this.initialVars = new TestData();
        }

        protected void resetTestRun() {
            this.resetTestRun(it -> it);
        }

        protected void resetTestRun(Function<TestRunBuilder, TestRunBuilder> function) {
            this.resetTestRun(new Scenario(this.testCase), function);
        }

        protected void resetTestRun(Scenario aScenario) {
            this.resetTestRun(aScenario, it -> it);
        }

        protected void resetTestRun(Scenario aScenario, Function<TestRunBuilder, TestRunBuilder> function) {
            this.scenario = aScenario;
            TestRunBuilder builder = new TestRunBuilder(testCase, scenario);
            this.target = function.apply(builder).createTestRun(log, driver, initialVars, listener);
        }
    }

    public static class FieldAccessor extends AbstractTestRunTest {

        @Before
        public void setUp() {
            this.resetTestRun();
        }

        @Test
        public void driver() {
            assertSame(this.driver, this.target.driver());
        }

        @Test
        public void log() {
            assertSame(this.log, this.target.log());
        }

        @Test
        public void getListener() {
            assertSame(this.listener, this.target.getListener());
        }

    }

    public static class VarDefault extends AbstractTestRunTest {

        @Before
        public void setUp() {
            this.resetTestRun();
        }

        @Test
        public void vars() {
            TestData actual = this.target.vars();
            assertEquals(7, actual.entrySet().size());
            assertEquals(Context.getBrowser(), actual.get("_browser"));
            assertEquals(Context.getBaseDirectory().getAbsolutePath(), actual.get("_baseDir"));
            assertEquals(Context.getDataSourceDirectory().getAbsolutePath(), actual.get("_dataSourceDir"));
            assertEquals(this.listener.getResultDir().getAbsolutePath(), actual.get("_resultDir"));
            assertEquals(this.listener.getScreenShotOutputDirectory().getAbsolutePath(), actual.get("_screenShotDir"));
            assertEquals(this.listener.getDownloadDirectory().getAbsolutePath(), actual.get("_downloadDir"));
            assertEquals(this.listener.getTemplateOutputDirectory().getAbsolutePath(), actual.get("_templateDir"));
        }

        @Test
        public void bindRuntimeVariables() {
            assertEquals(Context.getBrowser(), this.target.bindRuntimeVariables("${_browser}"));
        }

        @Test
        public void putVars() {
            assertNull(this.target.vars().get("key"));
            this.target.putVars("key", "test");
            assertEquals("test", target.vars().get("key"));
        }
    }

    public static class VarWithDataSource extends AbstractTestRunTest {

        @Before
        public void setUp() {
            this.initialVars = this.initialVars.add("bind", "bind parameter");
            this.resetTestRun();
        }

        @Test
        public void vars() {
            TestData actual = this.target.vars();
            assertEquals(8, actual.entrySet().size());
            assertEquals(Context.getBrowser(), actual.get("_browser"));
            assertEquals(Context.getBaseDirectory().getAbsolutePath(), actual.get("_baseDir"));
            assertEquals(Context.getDataSourceDirectory().getAbsolutePath(), actual.get("_dataSourceDir"));
            assertEquals(this.listener.getResultDir().getAbsolutePath(), actual.get("_resultDir"));
            assertEquals(this.listener.getScreenShotOutputDirectory().getAbsolutePath(), actual.get("_screenShotDir"));
            assertEquals(this.listener.getDownloadDirectory().getAbsolutePath(), actual.get("_downloadDir"));
            assertEquals(this.listener.getTemplateOutputDirectory().getAbsolutePath(), actual.get("_templateDir"));
            assertEquals("bind parameter", actual.get("bind"));
        }
    }

    public static class CurrentStep extends AbstractTestRunTest {

        private Step currentStep;

        @Before
        public void setUp() {
            this.currentStep = new StepBuilder(new SetElementText())
                    .put("text", "text：default")
                    .put("bindText", "text：${key}")
                    .put("true", "true")
                    .put("false", "false")
                    .put("bindTrue", "${keyTrue}")
                    .put("bindFalse", "${keyFalse}")
                    .put("locator", new Locator("id", "id"))
                    .put("locator2", new Locator("name", "name"))
                    .build();
            this.testCase = new TestCaseBuilder()
                    .addStep(this.currentStep)
                    .build();
            this.initialVars = this.initialVars
                    .add("keyFalse", "false")
                    .add("keyTrue", "true")
                    .add("key", "bind parameter");
            this.resetTestRun();
            this.target.toNextStepIndex();
        }

        @Test
        public void currentStep() {
            assertEquals(this.currentStep, this.target.currentStep());
        }

        @Test
        public void containsKey() {
            assertTrue(this.target.containsKey("text"));
        }

        @Test
        public void containsKey_notContainKey() {
            assertFalse(this.target.containsKey("text2"));
        }

        @Test
        public void getBoolean() {
            assertTrue(this.target.getBoolean("true"));
        }

        @Test
        public void getBoolean_False() {
            assertFalse(this.target.getBoolean("false"));
        }

        @Test
        public void getBoolean_bindTrue() {
            assertTrue(this.target.getBoolean("bindTrue"));
        }

        @Test
        public void getBoolean_bindFalse() {
            assertFalse(this.target.getBoolean("bindFalse"));
        }

        @Test
        public void text() {
            assertEquals("text：default", this.target.text());
        }

        @Test
        public void string() {
            assertEquals("text：bind parameter", this.target.string("bindText"));
        }

        @Test
        public void hasLocator() {
            assertTrue(this.target.hasLocator());
        }

        @Test
        public void locator() {
            assertEquals(new Locator("id", "id"), this.target.locator());
        }

        @Test
        public void locator1() {
            assertEquals(new Locator("name", "name"), this.target.locator("locator2"));
        }

    }

    public static class TestRunName extends AbstractTestRunTest {

        @Test
        public void getTestRunName_noName() {
            this.resetTestRun();
            assertEquals(TestCase.DEFAULT_SCRIPT_NAME, this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withName() {
            this.testCase = this.testCase.builder().setName("testRunName").build();
            this.resetTestRun();
            assertEquals("testRunName", this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withSuffix() {
            this.testCase = this.testCase.builder().setName("testRunName").build();
            this.resetTestRun(it -> it.addTestRunNameSuffix("_suffix"));
            assertEquals("testRunName_suffix", this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withPrefix() {
            this.testCase = this.testCase.builder().setName("testRunName").build();
            this.resetTestRun(it -> it.addTestRunNamePrefix("prefix_"));
            assertEquals("prefix_testRunName", this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withPrefixAndSuffix() {
            this.testCase = this.testCase.builder().setName("testRunName").build();
            this.resetTestRun(it -> it.addTestRunNamePrefix("prefix_").addTestRunNameSuffix("_suffix"));
            assertEquals("prefix_testRunName_suffix", this.target.getTestRunName());
        }

    }

    public static class StateChange extends AbstractTestRunTest {

        @Before
        public void setUp() {
            TestRunBuilder builder = new TestRunBuilder(testCase, scenario);
            this.target = builder.createTestRun(log, driver, initialVars, listener);
        }

        @Test
        public void isStopped() {
            assertFalse(this.target.isStopped());
        }

        @Test
        public void stop() {
            Assume.assumeFalse(this.target.isStopped());
            this.target.stop();
            assertTrue(this.target.isStopped());
        }

    }

    public static class WithNoStep extends AbstractTestRunTest {

        @Before
        public void setUp() {
            this.resetTestRun();
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).closeTestSuite();
        }

        @Test
        public void start() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(this.target.getTestRunName()), Mockito.any(TestData.class));

            assertTrue(this.target.start());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(this.target.getTestRunName()), Mockito.any(TestData.class));
        }

        @Test
        public void hasNext() {
            assertFalse(this.target.hasNext());
        }

        @Test
        public void stepRest() {
            assertFalse(this.target.stepRest());
        }

        @Test
        public void absent() {
            final IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
            expectedException.expect(AssertionError.class);
            expectedException.expectCause(CoreMatchers.is(illegalArgumentException));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            try {
                this.target.absent(illegalArgumentException);
            } finally {
                Mockito.verify(this.listener)
                        .closeTestSuite();
            }

        }

        @Test
        public void end() {
            Mockito.doNothing()
                    .when(this.listener)
                    .closeTestSuite();

            assertTrue(this.target.end(true));

            Mockito.verify(this.listener)
                    .closeTestSuite();
        }

        @Test
        public void end_false() {
            Mockito.doNothing()
                    .when(this.listener)
                    .closeTestSuite();

            assertFalse(this.target.end(false));

            Mockito.verify(this.listener)
                    .closeTestSuite();
        }

    }

    public static class WithStep extends AbstractTestRunTest {

        private Step step;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.resetTestRun();
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
            Mockito.verify(this.listener).closeTestSuite();
        }

        @Test
        public void startTest() {
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");

            this.target.toNextStepIndex();
            this.target.startTest();

            Mockito.verify(this.listener).startTest("name: ClickElement");
        }

        @Test
        public void processTestSuccess() {
            Mockito.doNothing().when(this.listener).endTest();

            this.target.toNextStepIndex();
            this.target.processTestSuccess();

            Mockito.verify(this.listener).endTest();
        }

        @Test
        public void processTestFailure() {

            expectedException.expect(AssertionError.class);
            expectedException.expectMessage("name: ClickElement failed.");
            Mockito.doNothing().when(this.listener).addFailure("name: ClickElement failed.");

            this.target.toNextStepIndex();
            try {
                this.target.processTestFailure();
            } finally {
                Mockito.verify(this.listener).addFailure("name: ClickElement failed.");
            }
        }

        @Test
        public void processTestError() {

            final IllegalArgumentException exception = new IllegalArgumentException();
            expectedException.expect(AssertionError.class);
            expectedException.expectCause(CoreMatchers.is(exception));
            Mockito.doNothing().when(this.listener).addError(exception);

            this.target.toNextStepIndex();
            try {
                this.target.processTestError(exception);
            } finally {
                Mockito.verify(this.listener).addError(exception);
            }
        }

        @Test
        public void hasNext() {
            assertTrue(this.target.hasNext());
        }

        @Test
        public void stepRest() {
            assertTrue(this.target.stepRest());
        }

        @Test
        public void next() {
            Mockito.doNothing().when(this.listener).skipTestIndex(1);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.next());

            Mockito.verify(this.listener).skipTestIndex(1);
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
        }

        @Test
        public void next_fail() {
            expectedException.expect(AssertionError.class);
            expectedException.expectMessage("name: ClickElement failed.");
            Mockito.doNothing().when(this.listener).skipTestIndex(1);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(false)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).addFailure("name: ClickElement failed.");

            try {
                this.target.next();
            } finally {
                Mockito.verify(this.listener).skipTestIndex(1);
                Mockito.verify(this.listener).startTest("name: ClickElement");
                Mockito.verify(this.step).run(this.target);
                Mockito.verify(this.listener).addFailure("name: ClickElement failed.");
            }
        }

        @Test
        public void next_failVerify() {
            this.step = Mockito.spy(new StepBuilder(new ElementAttribute().toVerify()).build());
            this.testCase = this.testCase.builder()
                    .clearStep()
                    .addStep(this.step)
                    .build();
            this.resetTestRun();
            Mockito.doNothing().when(this.listener).skipTestIndex(1);
            Mockito.doNothing().when(this.listener).startTest("verifyElementAttribute negated=false");
            Mockito.doReturn(false)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).addFailure("verifyElementAttribute negated=false failed.");

            assertFalse(this.target.next());

            Mockito.verify(this.listener).skipTestIndex(1);
            Mockito.verify(this.listener).startTest("verifyElementAttribute negated=false");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).addFailure("verifyElementAttribute negated=false failed.");
        }

        @Test
        public void next_error() {
            final NullPointerException nullPointerException = new NullPointerException();
            expectedException.expect(AssertionError.class);
            expectedException.expectCause(CoreMatchers.is(nullPointerException));
            Mockito.doNothing().when(this.listener).skipTestIndex(1);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doThrow(nullPointerException)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).addError(nullPointerException);
            try {
                this.target.next();
            } finally {
                Mockito.verify(this.listener).skipTestIndex(1);
                Mockito.verify(this.listener).startTest("name: ClickElement");
                Mockito.verify(this.step).run(this.target);
                Mockito.verify(this.listener).addError(nullPointerException);
            }
        }
    }

    public static class WithStepNoName extends AbstractTestRunTest {

        private Step step;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .put("text", "${bind}")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.initialVars = this.initialVars.add("bind", "parameter string");
            this.resetTestRun();
        }

        @Test
        public void startTest() {
            Mockito.doNothing().when(this.listener).startTest("ClickElement text=parameter string");

            this.target.toNextStepIndex();
            this.target.startTest();

            Mockito.verify(this.listener).startTest("ClickElement text=parameter string");
        }

        @Test
        public void processTestFailure() {

            expectedException.expect(AssertionError.class);

            Mockito.doNothing().when(this.listener).addFailure("ClickElement text=parameter string failed.");

            this.target.toNextStepIndex();
            try {
                this.target.processTestFailure();
            } finally {
                Mockito.verify(this.listener).addFailure("ClickElement text=parameter string failed.");
            }

        }

        @Test
        public void next() {
            Mockito.doNothing().when(this.listener).skipTestIndex(1);
            Mockito.doNothing().when(this.listener).startTest("ClickElement text=parameter string");
            Mockito.doReturn(true)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.next());

            Mockito.verify(this.listener).skipTestIndex(1);
            Mockito.verify(this.listener).startTest("ClickElement text=parameter string");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
        }
    }

    public static class WithChainRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private Step chainStep;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase").clearStep().addStep(this.chainStep).build();
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase));
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
        }

        @Test
        public void finish_failureMain() {
            expectedException.expect(AssertionError.class);
            expectedException.expectMessage("name: ClickElement failed.");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(false).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).addFailure("name: ClickElement failed.");

            try {
                target.finish();
            } finally {
                Mockito.verify(this.listener)
                        .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
                Mockito.verify(this.listener).startTest("name: ClickElement");
                Mockito.verify(this.step).run(this.target);
                Mockito.verify(this.listener, Mockito.times(1)).endTest();
                Mockito.verify(this.listener, Mockito.times(1)).closeTestSuite();
                Mockito.verify(this.listener).addFailure("name: ClickElement failed.");
                Mockito.verify(this.listener, Mockito.never())
                        .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
                Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement");
                Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            }
        }

        @Test
        public void finish_failureChain() {
            expectedException.expect(AssertionError.class);
            expectedException.expectMessage("chain: ClickElement failed.");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(false).when(this.chainStep).run(Mockito.any());
            Mockito.doNothing().when(this.listener).addFailure("chain: ClickElement failed.");
            try {
                target.finish();
            } finally {
                Mockito.verify(this.listener)
                        .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
                Mockito.verify(this.listener).startTest("name: ClickElement");
                Mockito.verify(this.step).run(this.target);
                Mockito.verify(this.listener).endTest();
                Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
                Mockito.verify(this.listener)
                        .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
                Mockito.verify(this.listener).startTest("chain: ClickElement");
                Mockito.verify(this.chainStep).run(Mockito.any());
                Mockito.verify(this.listener, Mockito.times(1)).addFailure("chain: ClickElement failed.");
            }
        }

        @Test
        public void next() {
            Mockito.doNothing().when(this.listener).skipTestIndex(1);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.next());

            Mockito.verify(this.listener).skipTestIndex(1);
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
        }
    }

    public static class WithAspect extends AbstractTestRunTest {

        private TestCase chainCase;

        private Step chainStep;

        private Step aspectBeforeStep;

        private Step aspectAfterStep;

        private Interceptor interceptor;

        @Mock
        private TestRunListenerImpl adviseListener;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase").clearStep().addStep(this.chainStep).build();
            this.aspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.aspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.interceptor = Mockito.spy(new Interceptor(
                    it -> "name".equals(it.getName())
                    , Lists.newArrayList(this.aspectBeforeStep)
                    , Lists.newArrayList(this.aspectAfterStep)
                    , Lists.newArrayList()));
            final Aspect aspect = new Aspect(Lists.newArrayList(interceptor));
            Mockito.doReturn(new File(".", "result")).when(this.adviseListener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.adviseListener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.adviseListener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.adviseListener).getDownloadDirectory();
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).addAspect(aspect));
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doReturn(this.adviseListener)
                    .when(this.interceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.aspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.adviseListener).endTest();
            Mockito.doNothing().when(this.adviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.aspectAfterStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
            Mockito.verify(this.interceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.verify(this.adviseListener).startTest("SetElementText");
            Mockito.verify(this.aspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.adviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.adviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.verify(this.adviseListener).startTest("DoubleClickElement");
            Mockito.verify(this.aspectAfterStep).run(Mockito.any());
        }

    }

    public static class WithContextAspect extends AbstractTestRunTest {

        private TestCase chainCase;

        private Step chainStep;

        private Step aspectBeforeStep;

        private Step aspectAfterStep;

        private Interceptor interceptor;

        @Mock
        private TestRunListenerImpl adviseListener;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase").clearStep().addStep(this.chainStep).build();
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase));
            this.aspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.aspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.interceptor = Mockito.spy(new Interceptor(
                    it -> "chain".equals(it.getName())
                    , Lists.newArrayList(this.aspectBeforeStep)
                    , Lists.newArrayList(this.aspectAfterStep)
                    , Lists.newArrayList()));
            final Aspect aAspect = new Aspect(Lists.newArrayList(interceptor));
            Context.getInstance().setAspect(aAspect);
            Mockito.doReturn(new File(".", "result")).when(this.adviseListener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.adviseListener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.adviseListener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.adviseListener).getDownloadDirectory();
        }

        @After
        public void tearDown() {
            Context.getInstance().setAspect(new Aspect());
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.doReturn(this.adviseListener)
                    .when(this.interceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.aspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.adviseListener).endTest();
            Mockito.doNothing().when(this.adviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.aspectAfterStep).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
            Mockito.verify(this.interceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.verify(this.adviseListener).startTest("SetElementText");
            Mockito.verify(this.aspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.adviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.adviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.verify(this.adviseListener).startTest("DoubleClickElement");
            Mockito.verify(this.aspectAfterStep).run(Mockito.any());
        }

    }

    public static class WithAspectAndContextAspect extends AbstractTestRunTest {

        private TestCase chainCase;

        private Step chainStep;

        private Step aspectBeforeStep;

        private Step aspectAfterStep;

        private Interceptor interceptor;

        private Step contextAspectBeforeStep;

        private Step contextAspectAfterStep;

        private Interceptor contextInterceptor;

        @Mock
        private TestRunListenerImpl adviseListener;

        @Mock
        private TestRunListenerImpl contextAdviseListener;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase").clearStep().addStep(this.chainStep).build();
            this.aspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.aspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.interceptor = Mockito.spy(new Interceptor(
                    it -> "name".equals(it.getName())
                    , Lists.newArrayList(this.aspectBeforeStep)
                    , Lists.newArrayList(this.aspectAfterStep)
                    , Lists.newArrayList()));
            final Aspect aspect = new Aspect(Lists.newArrayList(interceptor));
            Mockito.doReturn(new File(".", "result")).when(this.adviseListener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.adviseListener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.adviseListener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.adviseListener).getDownloadDirectory();
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).addAspect(aspect));
            this.contextAspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.contextAspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.contextInterceptor = Mockito.spy(new Interceptor(
                    it -> "chain".equals(it.getName())
                    , Lists.newArrayList(this.contextAspectBeforeStep)
                    , Lists.newArrayList(this.contextAspectAfterStep)
                    , Lists.newArrayList()));
            final Aspect aAspect = new Aspect(Lists.newArrayList(contextInterceptor));
            Context.getInstance().setAspect(aAspect);
            Mockito.doReturn(new File(".", "result")).when(this.contextAdviseListener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.contextAdviseListener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.contextAdviseListener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.contextAdviseListener).getDownloadDirectory();
        }

        @After
        public void tearDown() {
            Context.getInstance().setAspect(new Aspect());
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doReturn(this.adviseListener)
                    .when(this.interceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.aspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.adviseListener).endTest();
            Mockito.doNothing().when(this.adviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.aspectAfterStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.doReturn(this.contextAdviseListener)
                    .when(this.contextInterceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.contextAdviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.contextAdviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.contextAspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.contextAdviseListener).endTest();
            Mockito.doNothing().when(this.contextAdviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.contextAdviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.contextAdviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.contextAspectAfterStep).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
            Mockito.verify(this.interceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.verify(this.adviseListener).startTest("SetElementText");
            Mockito.verify(this.aspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.adviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.adviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.verify(this.adviseListener).startTest("DoubleClickElement");
            Mockito.verify(this.aspectAfterStep).run(Mockito.any());
            Mockito.verify(this.contextInterceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.contextAdviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_before"), Mockito.any(TestData.class));
            Mockito.verify(this.contextAdviseListener).startTest("SetElementText");
            Mockito.verify(this.contextAspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.contextAdviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.contextAdviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.contextAdviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_ClickElement_aspect_after"), Mockito.any(TestData.class));
            Mockito.verify(this.contextAdviseListener).startTest("DoubleClickElement");
            Mockito.verify(this.contextAspectAfterStep).run(Mockito.any());
        }

    }

    public static class WithSkippableChainRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private Step chainStep;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase")
                    .setSkip("${skip}")
                    .clearStep().addStep(this.chainStep).build();
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skip", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
        }

        @Test
        public void finish_chainSkip() {
            this.initialVars = this.initialVars.add("skip", "true");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
            Mockito.verify(this.listener).closeTestSuite();
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
        }
    }

    public static class WithDataDrivenChainRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private Step chainStep;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase")
                    .setDataSource(new Csv(), Map.of("path", this.getClass().getResource("test.csv").getFile()))
                    .clearStep().addStep(this.chainStep).build();
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase));
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.times(3)).closeTestSuite();
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.listener, Mockito.times(2)).startTest("chain: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(3)).endTest();
        }

    }

    public static class WithChainRuns extends AbstractTestRunTest {

        private TestCase chainCase;

        private TestCase chainCase2;

        private Step chainStep;

        private Step chainStep2;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getClass().getResource("test.csv").getFile()))
                    .clearStep().addStep(this.chainStep).build();
            this.chainStep2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.testCase.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .clearStep().addStep(this.chainStep2).build();
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chainStep2).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(4)).endTest();
            Mockito.verify(this.listener, Mockito.times(4)).closeTestSuite();
        }

        @Test
        public void finish_skipMiddleCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chainStep2).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
        }

        @Test
        public void finish_skipLastCase() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "true");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chainStep2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(3)).endTest();
            Mockito.verify(this.listener, Mockito.times(3)).closeTestSuite();
        }

        @Test
        public void finish_skipAllChainCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "true");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chainStep2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener).endTest();
            Mockito.verify(this.listener).closeTestSuite();
        }

        @Test
        public void finish_varTakeOverAndOverride() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());

            this.target.putVars("key", "change");
            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chainStep2).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(4)).endTest();
            Mockito.verify(this.listener, Mockito.times(4)).closeTestSuite();
        }

    }

    public static class WithNestedChainRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private TestCase chainCase2;

        private Step chainStep;

        private Step chainStep2;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getClass().getResource("test.csv").getFile()))
                    .isNestedChain(true)
                    .clearStep().addStep(this.chainStep).build();
            this.chainStep2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.testCase.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .clearStep().addStep(this.chainStep2).build();
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chainStep2, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(5)).endTest();
            Mockito.verify(this.listener, Mockito.times(5)).closeTestSuite();
        }

        @Test
        public void finish_skipMiddleCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chainStep2).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
        }

        @Test
        public void finish_skipLastCase() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "true");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chainStep2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(3)).endTest();
            Mockito.verify(this.listener, Mockito.times(3)).closeTestSuite();
        }

        @Test
        public void finish_skipAllChainCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "true");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chainStep2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener).endTest();
            Mockito.verify(this.listener).closeTestSuite();
        }

        @Test
        public void finish_varTakeOverAndOverride() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");

            this.target.putVars("key", "change");
            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chainStep2, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(5)).endTest();
            Mockito.verify(this.listener, Mockito.times(5)).closeTestSuite();
        }

    }

    public static class WithBreakNestedChainRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private TestCase chainCase2;

        private TestCase chainCase3;

        private Step chainStep;

        private Step chainStep2;

        private Step chainStep3;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getClass().getResource("test.csv").getFile()))
                    .isNestedChain(true)
                    .clearStep().addStep(this.chainStep).build();
            this.chainStep2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.testCase.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .clearStep().addStep(this.chainStep2).build();
            this.chainStep3 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain3")
                    .put("key", "${key}")
                    .build());
            this.chainCase3 = this.testCase.builder().setName("chainCase3")
                    .isBreakNestedChain(true)
                    .clearStep().addStep(this.chainStep3).build();
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase)
                    .appendNewChain(this.testCase, this.chainCase)
                    .appendNewChain(this.chainCase, this.chainCase2)
                    .appendNewChain(this.chainCase2, this.chainCase3));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase3), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase3"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain3: ClickElement key=2");
            Mockito.doReturn(true).when(this.chainStep3).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase3), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase3"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chainStep2, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain3: ClickElement key=2");
            Mockito.verify(this.chainStep3, Mockito.times(1)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(6)).endTest();
            Mockito.verify(this.listener, Mockito.times(6)).closeTestSuite();
        }
    }

    public static class WithBreakNestedChainSameCase extends AbstractTestRunTest {

        private TestCase chainCase;

        private TestCase chainCase2;

        private Step chainStep;

        private Step chainStep2;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.testCase = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.testCase.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getClass().getResource("test.csv").getFile()))
                    .isNestedChain(true)
                    .clearStep().addStep(this.chainStep).build();
            this.chainStep2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.testCase.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .isBreakNestedChain(true)
                    .clearStep().addStep(this.chainStep2).build();
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun(new Scenario(testCase).appendNewChain(this.testCase, this.chainCase).appendNewChain(this.chainCase, this.chainCase2));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.doReturn(true).when(this.chainStep2).run(Mockito.any());

            assertTrue(target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.testCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_1"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.eq(this.chainCase2), Mockito.eq(TestCase.DEFAULT_SCRIPT_NAME + "_chainCase_row_2_chainCase2"), Mockito.any(TestData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chainStep2, Mockito.times(1)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(4)).endTest();
            Mockito.verify(this.listener, Mockito.times(4)).closeTestSuite();
        }

    }

    public static class Quit extends AbstractTestRunTest {
        @Test
        public void quit() {
            TestRunBuilder builder = new TestRunBuilder(testCase, scenario);
            this.target = builder.createTestRun(log, driver, initialVars, listener);
            this.target.quit();
            Mockito.verify(this.log).debug("Quitting driver.");
            Mockito.verify(this.driver).quit();
        }

        @Test
        public void quit_withParentDriver() {
            this.testCase = this.testCase.builder().usePreviousDriverAndVars(true).build();
            resetTestRun();
            this.target.quit();
            Mockito.verify(this.driver, Mockito.never()).quit();
        }

    }
}
