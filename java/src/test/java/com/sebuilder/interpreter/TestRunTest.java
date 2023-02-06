package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.datasource.Csv;
import com.sebuilder.interpreter.report.TestRunListenerImpl;
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
import java.net.URL;
import java.util.Map;
import java.util.Objects;
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
        TestCase head;
        InputData initialVars;
        TestCaseChains chains;
        Aspect aspect;
        TestRun target;

        @Before
        public void setUpStub() {
            Context.getInstance().setBrowser("Chrome");
            Context.getInstance().setDataSourceDirectory("datasource");
            Context.getInstance().setDataSourceEncoding("UTF-8");
            Context.getInstance().setAspect(new Aspect());
            Mockito.doReturn(new File(".", "result")).when(this.listener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.listener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.listener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.listener).getDownloadDirectory();
            this.head = new TestCaseBuilder().build();
            this.initialVars = new InputData();
            this.chains = new TestCaseChains();
            this.aspect = new Aspect();
        }

        protected void resetTestRun() {
            this.resetTestRun(it -> it);
        }

        protected void resetTestRun(final Function<TestRunBuilder, TestRunBuilder> function) {
            this.head = this.head.builder().setAspect(this.aspect).setChains(this.chains).build();
            final TestRunBuilder builder = new TestRunBuilder(this.head);
            this.target = function.apply(builder).createTestRun(this.log, this.driver, this.initialVars, this.listener);
        }

        protected URL getResourceUrl() {
            return Objects.requireNonNull(this.getClass().getResource("test.csv"));
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
            final InputData actual = this.target.vars();
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
            assertEquals("test", this.target.vars().get("key"));
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
            final InputData actual = this.target.vars();
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
            this.head = new TestCaseBuilder()
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
            assertEquals(ScriptFile.Type.TEST.getDefaultName(), this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withName() {
            this.head = this.head.builder().setName("testRunName").build();
            this.resetTestRun();
            assertEquals("testRunName", this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withSuffix() {
            this.head = this.head.builder().setName("testRunName").build();
            this.resetTestRun(it -> it.addTestRunNameSuffix("_suffix"));
            assertEquals("testRunName_suffix", this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withPrefix() {
            this.head = this.head.builder().setName("testRunName").build();
            this.resetTestRun(it -> it.addTestRunNamePrefix("prefix_"));
            assertEquals("prefix_testRunName", this.target.getTestRunName());
        }

        @Test
        public void getTestRunName_withPrefixAndSuffix() {
            this.head = this.head.builder().setName("testRunName").build();
            this.resetTestRun(it -> it.addTestRunNamePrefix("prefix_").addTestRunNameSuffix("_suffix"));
            assertEquals("prefix_testRunName_suffix", this.target.getTestRunName());
        }

    }

    public static class StateChange extends AbstractTestRunTest {

        @Before
        public void setUp() {
            final TestRunBuilder builder = new TestRunBuilder(this.head.map(it -> it.setChains(this.chains)));
            this.target = builder.createTestRun(this.log, this.driver, this.initialVars, this.listener);
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
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.verify(this.listener).closeTestSuite();
        }

        @Test
        public void start() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(this.target.getTestRunName()), Mockito.any(InputData.class));

            assertTrue(this.target.start());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(this.target.getTestRunName()), Mockito.any(InputData.class));
        }

        @Test
        public void hasNext() {
            assertFalse(this.target.hasNext());
        }

        @Test
        public void absent() {
            final IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
            this.expectedException.expect(AssertionError.class);
            this.expectedException.expectCause(CoreMatchers.is(illegalArgumentException));
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
            this.head = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.resetTestRun();
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
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
            this.target.processTestSuccess(true);

            Mockito.verify(this.listener).endTest();
        }

        @Test
        public void processTestFailure() {

            this.expectedException.expect(AssertionError.class);
            this.expectedException.expectMessage("name: ClickElement failed.");
            Mockito.doNothing().when(this.listener).addFailure("name: ClickElement failed.");

            this.target.toNextStepIndex();
            try {
                this.target.processTestFailure(true);
            } finally {
                Mockito.verify(this.listener).addFailure("name: ClickElement failed.");
            }
        }

        @Test
        public void processTestError() {

            final IllegalArgumentException exception = new IllegalArgumentException();
            this.expectedException.expect(AssertionError.class);
            this.expectedException.expectCause(CoreMatchers.is(exception));
            Mockito.doNothing().when(this.listener).addError(exception);

            this.target.toNextStepIndex();
            try {
                throw this.target.processTestError(exception);
            } finally {
                Mockito.verify(this.listener).addError(exception);
            }
        }

        @Test
        public void hasNext() {
            assertTrue(this.target.hasNext());
        }

        @Test
        public void next() {
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.next());

            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
        }

        @Test
        public void next_fail() {
            this.expectedException.expect(AssertionError.class);
            this.expectedException.expectMessage("name: ClickElement failed.");
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(false)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).addFailure("name: ClickElement failed.");

            try {
                this.target.next();
            } finally {
                Mockito.verify(this.listener).startTest("name: ClickElement");
                Mockito.verify(this.step).run(this.target);
                Mockito.verify(this.listener).addFailure("name: ClickElement failed.");
            }
        }

        @Test
        public void next_failVerify() {
            this.step = Mockito.spy(new StepBuilder(new ElementAttribute().toVerify()).build());
            this.head = this.head.builder()
                    .clearStep()
                    .addStep(this.step)
                    .build();
            this.resetTestRun();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("verifyElementAttribute negated=false");
            Mockito.doReturn(false)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).addFailure("verifyElementAttribute negated=false failed.");

            assertFalse(this.target.next());

            Mockito.verify(this.listener).startTest("verifyElementAttribute negated=false");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).addFailure("verifyElementAttribute negated=false failed.");
        }

        @Test
        public void next_error() {
            final NullPointerException nullPointerException = new NullPointerException();
            this.expectedException.expect(AssertionError.class);
            this.expectedException.expectCause(CoreMatchers.is(nullPointerException));
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doThrow(nullPointerException)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).addError(nullPointerException);
            try {
                this.target.next();
            } finally {
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
            this.head = new TestCaseBuilder()
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

            this.expectedException.expect(AssertionError.class);

            Mockito.doNothing().when(this.listener).addFailure("ClickElement text=parameter string failed.");

            this.target.toNextStepIndex();
            try {
                this.target.processTestFailure(true);
            } finally {
                Mockito.verify(this.listener).addFailure("ClickElement text=parameter string failed.");
            }

        }

        @Test
        public void next() {
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("ClickElement text=parameter string");
            Mockito.doReturn(true)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.next());

            Mockito.verify(this.listener).startTest("ClickElement text=parameter string");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
        }
    }

    public static class WithChainRun extends AbstractTestRunTest {

        private Step chainStep;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            final TestCase chainCase = this.head.builder().setName("chainCase").clearStep().addStep(this.chainStep).build();
            this.chains = this.chains.append(chainCase);
            this.resetTestRun();
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
        }

        @Test
        public void finish_failureMain() {
            this.expectedException.expect(AssertionError.class);
            this.expectedException.expectMessage("name: ClickElement failed.");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(false).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).addFailure("name: ClickElement failed.");

            try {
                this.target.finish();
            } finally {
                Mockito.verify(this.listener)
                        .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
                Mockito.verify(this.listener).startTest("name: ClickElement");
                Mockito.verify(this.step).run(this.target);
                Mockito.verify(this.listener, Mockito.times(1)).endTest();
                Mockito.verify(this.listener, Mockito.times(1)).closeTestSuite();
                Mockito.verify(this.listener).addFailure("name: ClickElement failed.");
                Mockito.verify(this.listener, Mockito.never())
                        .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_chainCase"), Mockito.any(InputData.class));
                Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement");
                Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            }
        }

        @Test
        public void finish_failureChain() {
            this.expectedException.expect(AssertionError.class);
            this.expectedException.expectMessage("chain: ClickElement failed.");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(false).when(this.chainStep).run(Mockito.any());
            Mockito.doNothing().when(this.listener).addFailure("chain: ClickElement failed.");
            try {
                this.target.finish();
            } finally {
                Mockito.verify(this.listener)
                        .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
                Mockito.verify(this.listener).startTest("name: ClickElement");
                Mockito.verify(this.step).run(this.target);
                Mockito.verify(this.listener).endTest();
                Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
                Mockito.verify(this.listener)
                        .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
                Mockito.verify(this.listener).startTest("chain: ClickElement");
                Mockito.verify(this.chainStep).run(Mockito.any());
                Mockito.verify(this.listener, Mockito.times(1)).addFailure("chain: ClickElement failed.");
            }
        }

        @Test
        public void next() {
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true)
                    .when(this.step)
                    .run(this.target);
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.next());

            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener).endTest();
        }
    }

    public static class WithAspect extends AbstractTestRunTest {

        private Step chainStep;

        private Step aspectBeforeStep;

        private Step aspectAfterStep;

        private ExtraStepExecuteInterceptor interceptor;

        @Mock
        private TestRunListenerImpl adviseListener;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            final TestCase chainCase = this.head.builder().setName("chainCase").clearStep().addStep(this.chainStep).build();
            this.chains = this.chains.append(chainCase);
            this.aspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.aspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.interceptor = Mockito.spy(new ExtraStepExecuteInterceptor(
                    (step, input) -> "name".equals(step.name())
                    , this.aspectBeforeStep.toTestCase()
                    , this.aspectAfterStep.toTestCase()
                    , new TestCaseBuilder().build()));
            final Aspect aspect = new Aspect(Lists.newArrayList(this.interceptor));
            Mockito.doReturn(new File(".", "result")).when(this.adviseListener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.adviseListener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.adviseListener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.adviseListener).getDownloadDirectory();
            this.aspect = aspect;
            this.resetTestRun();
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doReturn(this.adviseListener)
                    .when(this.interceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.aspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.adviseListener).endTest();
            Mockito.doNothing().when(this.adviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_after"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.aspectAfterStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
            Mockito.verify(this.interceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.verify(this.adviseListener).startTest("SetElementText");
            Mockito.verify(this.aspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.adviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.adviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_after"), Mockito.any(InputData.class));
            Mockito.verify(this.adviseListener).startTest("DoubleClickElement");
            Mockito.verify(this.aspectAfterStep).run(Mockito.any());
        }

    }

    public static class WithContextAspect extends AbstractTestRunTest {

        private Step chainStep;

        private Step aspectBeforeStep;

        private Step aspectAfterStep;

        private ExtraStepExecuteInterceptor interceptor;

        @Mock
        private TestRunListenerImpl adviseListener;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            final TestCase chainCase = this.head.builder().setName("chainCase").clearStep().addStep(this.chainStep).build();
            this.chains = this.chains.append(chainCase);
            this.resetTestRun();
            this.aspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.aspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.interceptor = Mockito.spy(new ExtraStepExecuteInterceptor(
                    (step, input) -> "chain".equals(step.name())
                    , this.aspectBeforeStep.toTestCase()
                    , this.aspectAfterStep.toTestCase()
                    , new TestCaseBuilder().build()));
            final Aspect aAspect = new Aspect(Lists.newArrayList(this.interceptor));
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
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.doReturn(this.adviseListener)
                    .when(this.interceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.aspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.adviseListener).endTest();
            Mockito.doNothing().when(this.adviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_after"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.aspectAfterStep).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
            Mockito.verify(this.interceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.verify(this.adviseListener).startTest("SetElementText");
            Mockito.verify(this.aspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.adviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.adviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_after"), Mockito.any(InputData.class));
            Mockito.verify(this.adviseListener).startTest("DoubleClickElement");
            Mockito.verify(this.aspectAfterStep).run(Mockito.any());
        }

    }

    public static class WithAspectAndContextAspect extends AbstractTestRunTest {

        private Step chainStep;

        private Step aspectBeforeStep;

        private Step aspectAfterStep;

        private ExtraStepExecuteInterceptor interceptor;

        private Step contextAspectBeforeStep;

        private Step contextAspectAfterStep;

        private ExtraStepExecuteInterceptor contextInterceptor;

        @Mock
        private TestRunListenerImpl adviseListener;

        @Mock
        private TestRunListenerImpl contextAdviseListener;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chains = this.chains.append(this.head
                    .builder()
                    .setName("chainCase")
                    .clearStep()
                    .addStep(this.chainStep)
                    .build());
            this.aspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.aspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.interceptor = Mockito.spy(new ExtraStepExecuteInterceptor(
                    (step, input) -> "name".equals(step.name())
                    , this.aspectBeforeStep.toTestCase()
                    , this.aspectAfterStep.toTestCase()
                    , new TestCaseBuilder().build()));
            final Aspect aspect = new Aspect(Lists.newArrayList(this.interceptor));
            Mockito.doReturn(new File(".", "result")).when(this.adviseListener).getResultDir();
            Mockito.doReturn(new File(".", "screenshot")).when(this.adviseListener).getScreenShotOutputDirectory();
            Mockito.doReturn(new File(".", "template")).when(this.adviseListener).getTemplateOutputDirectory();
            Mockito.doReturn(new File(".", "download")).when(this.adviseListener).getDownloadDirectory();
            this.aspect = aspect;
            this.resetTestRun();
            this.contextAspectBeforeStep = Mockito.spy(new StepBuilder(new SetElementText()).build());
            this.contextAspectAfterStep = Mockito.spy(new StepBuilder(new DoubleClickElement()).build());
            this.contextInterceptor = Mockito.spy(new ExtraStepExecuteInterceptor(
                    (step, input) -> "chain".equals(step.name())
                    , this.contextAspectBeforeStep.toTestCase()
                    , this.contextAspectAfterStep.toTestCase()
                    , new TestCaseBuilder().build()));
            final Aspect aAspect = new Aspect(Lists.newArrayList(this.contextInterceptor));
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
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doReturn(this.adviseListener)
                    .when(this.interceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.aspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.adviseListener).endTest();
            Mockito.doNothing().when(this.adviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.adviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_after"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.adviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.aspectAfterStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.doReturn(this.contextAdviseListener)
                    .when(this.contextInterceptor)
                    .createAdviseListener(Mockito.any(TestRun.class));
            Mockito.doReturn(true)
                    .when(this.contextAdviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.contextAdviseListener).startTest("SetElementText");
            Mockito.doReturn(true).when(this.contextAspectBeforeStep).run(Mockito.any());
            Mockito.doNothing().when(this.contextAdviseListener).endTest();
            Mockito.doNothing().when(this.contextAdviseListener).closeTestSuite();
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.contextAdviseListener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_after"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.contextAdviseListener).startTest("DoubleClickElement");
            Mockito.doReturn(true).when(this.contextAspectAfterStep).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(this.target);
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
            Mockito.verify(this.interceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.verify(this.adviseListener).startTest("SetElementText");
            Mockito.verify(this.aspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.adviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.adviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.adviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_ClickElement_after"), Mockito.any(InputData.class));
            Mockito.verify(this.adviseListener).startTest("DoubleClickElement");
            Mockito.verify(this.aspectAfterStep).run(Mockito.any());
            Mockito.verify(this.contextInterceptor, Mockito.times(2)).createAdviseListener(Mockito.any(TestRun.class));
            Mockito.verify(this.contextAdviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_before"), Mockito.any(InputData.class));
            Mockito.verify(this.contextAdviseListener).startTest("SetElementText");
            Mockito.verify(this.contextAspectBeforeStep).run(Mockito.any());
            Mockito.verify(this.contextAdviseListener, Mockito.times(2)).endTest();
            Mockito.verify(this.contextAdviseListener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.contextAdviseListener).openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_0_ClickElement_after"), Mockito.any(InputData.class));
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
            this.head = new TestCaseBuilder()
                    .setName("suite")
                    .build();
            TestCase chainStart = new TestCaseBuilder()
                    .setName("chainStart")
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            this.chainCase = this.head.builder().setName("chainCase")
                    .setSkip("${skip}")
                    .clearStep().addStep(this.chainStep).build();
            chainStart = chainStart.map(it -> it.addChain(this.chainCase).isChainTakeOverLastRun(true));
            this.chains = this.chains.append(chainStart);
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skip", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(3)).closeTestSuite();
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep).run(Mockito.any());
        }

        @Test
        public void finish_chainSkip() {
            this.initialVars = this.initialVars.add("skip", "true");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
        }
    }

    public static class WithDataDrivenChainRun extends AbstractTestRunTest {

        private Step chainStep;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = new TestCaseBuilder()
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .build());
            final TestCase chainCase = this.head.builder().setName("chainCase")
                    .setDataSource(new Csv(), Map.of("path", this.getResourceUrl().getFile()))
                    .clearStep().addStep(this.chainStep).build();
            this.chains = this.chains.append(chainCase);
            this.resetTestRun();
        }

        @Test
        public void finish() {
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(this.target);
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_row_2"), Mockito.any(InputData.class));

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName()), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq(ScriptFile.Type.TEST.getDefaultName() + "_0_chainCase_row_2"), Mockito.any(InputData.class));
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

        private Step chain2;

        @Before
        public void setUp() {
            this.head = new TestCaseBuilder().setName("suite").build();
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            TestCase chainStart = new TestCaseBuilder()
                    .setName("chainStart")
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.head.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getResourceUrl().getFile()))
                    .addStep(this.chainStep)
                    .build();
            this.chain2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.head.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .addStep(this.chain2)
                    .build();
            chainStart = chainStart.map(it -> it.addChain(this.chainCase).addChain(this.chainCase2).isChainTakeOverLastRun(true));
            this.chains = this.chains.append(chainStart);
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.doReturn(true).when(this.chain2).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chain2).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(4)).endTest();
            Mockito.verify(this.listener, Mockito.times(5)).closeTestSuite();
        }

        @Test
        public void finish_skipMiddleCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.doReturn(true).when(this.chain2).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chain2).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(2)).endTest();
            Mockito.verify(this.listener, Mockito.times(3)).closeTestSuite();
        }

        @Test
        public void finish_skipLastCase() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "true");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chain2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(3)).endTest();
            Mockito.verify(this.listener, Mockito.times(4)).closeTestSuite();
        }

        @Test
        public void finish_skipAllChainCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "true");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chain2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
        }

        @Test
        public void finish_varTakeOverAndOverride() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.doReturn(true).when(this.chain2).run(Mockito.any());

            this.target.putVars("key", "change");
            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chain2).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(4)).endTest();
            Mockito.verify(this.listener, Mockito.times(5)).closeTestSuite();
        }

    }

    public static class WithNestedChainRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private TestCase chainCase2;

        private Step chainStep;

        private Step chain2;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = new TestCaseBuilder()
                    .setName("suite")
                    .build();
            TestCase chainStart = new TestCaseBuilder()
                    .setName("chainStart")
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.head.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getResourceUrl().getFile()))
                    .isNestedChain(true)
                    .addStep(this.chainStep).build();
            this.chain2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.head.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .addStep(this.chain2)
                    .build();
            this.chainCase = this.chainCase.map(it -> it.addChain(this.chainCase2).isChainTakeOverLastRun(true));
            chainStart = chainStart.map(it -> it.addChain(this.chainCase));
            this.chains = this.chains.append(chainStart);
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.doReturn(true).when(this.chain2).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chain2, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(5)).endTest();
            Mockito.verify(this.listener, Mockito.times(6)).closeTestSuite();
        }

        @Test
        public void finish_skipNestedHeaderCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chain2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(1)).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
        }

        @Test
        public void finish_skipLastCase() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "true");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chain2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(3)).endTest();
            Mockito.verify(this.listener, Mockito.times(4)).closeTestSuite();
        }

        @Test
        public void finish_skipAllChainCase() {
            this.initialVars = this.initialVars.add("skipChain1", "true").add("skipChain2", "true");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener, Mockito.never())
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener, Mockito.never()).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.never()).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chain2, Mockito.never()).run(Mockito.any());
            Mockito.verify(this.listener).endTest();
            Mockito.verify(this.listener, Mockito.times(2)).closeTestSuite();
        }

        @Test
        public void finish_varTakeOverAndOverride() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.doReturn(true).when(this.chain2).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");

            this.target.putVars("key", "change");
            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chain2, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(5)).endTest();
            Mockito.verify(this.listener, Mockito.times(6)).closeTestSuite();
        }

    }

    public static class WithBreakNestedChainRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private TestCase chainCase2;

        private TestCase chainCase3;

        private Step chainStep;

        private Step chain2;

        private Step chain3;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = new TestCaseBuilder()
                    .setName("suite")
                    .build();
            TestCase chainStart = new TestCaseBuilder()
                    .setName("chainStart")
                    .addStep(this.step)
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.head.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getResourceUrl().getFile()))
                    .isNestedChain(true)
                    .addStep(this.chainStep).build();
            this.chain2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.head.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .addStep(this.chain2)
                    .build();
            this.chain3 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain3")
                    .put("key", "${key}")
                    .build());
            this.chainCase3 = this.head.builder().setName("chainCase3")
                    .isBreakNestedChain(true)
                    .addStep(this.chain3)
                    .build();
            this.chainCase = this.chainCase.map(it -> it.addChain(this.chainCase2).isChainTakeOverLastRun(true));
            chainStart = chainStart.map(it -> it.addChain(this.chainCase).addChain(this.chainCase3).isChainTakeOverLastRun(true));
            this.chains = this.chains.append(chainStart);
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.doReturn(true).when(this.chain2).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase3"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain3: ClickElement key=2");
            Mockito.doReturn(true).when(this.chain3).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_1_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_chainCase_row_2_0_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase3"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=2");
            Mockito.verify(this.chain2, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain3: ClickElement key=2");
            Mockito.verify(this.chain3, Mockito.times(1)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(6)).endTest();
            Mockito.verify(this.listener, Mockito.times(7)).closeTestSuite();
        }
    }

    public static class WithChainIncludeSuiteRun extends AbstractTestRunTest {

        private TestCase chainCase;

        private TestCase chainSuiteHeader;

        private TestCase chainCase2;

        private TestCase chainCase3;

        private Step chainStep;

        private Step chain2;

        private Step chain3;

        @Before
        public void setUp() {
            this.step = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("name")
                    .build());
            this.head = TestCaseBuilder.suite(null)
                    .setName("suite")
                    .build();
            TestCase chainStart = new TestCaseBuilder()
                    .setName("chainStart")
                    .addStep(this.step)
                    .build();
            this.chainSuiteHeader = TestCaseBuilder.suite(null)
                    .setName("suite2")
                    .build();
            this.chainStep = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain")
                    .put("key", "${key}")
                    .build());
            this.chainCase = this.head.builder().setName("chainCase")
                    .setSkip("${skipChain1}")
                    .setDataSource(new Csv(), Map.of("path", this.getResourceUrl().getFile()))
                    .addStep(this.chainStep).build();
            this.chain2 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain2")
                    .put("key", "${key}")
                    .build());
            this.chainCase2 = this.head.builder().setName("chainCase2")
                    .setSkip("${skipChain2}")
                    .addStep(this.chain2)
                    .build();
            this.chain3 = Mockito.spy(new StepBuilder(new ClickElement())
                    .name("chain3")
                    .put("key", "${key}")
                    .build());
            this.chainCase3 = this.head.builder().setName("chainCase3")
                    .addStep(this.chain3)
                    .build();
            this.chainSuiteHeader = this.chainSuiteHeader.map(it -> it.addChain(this.chainCase).addChain(this.chainCase2));
            chainStart = chainStart.map(it -> it.addChain(this.chainSuiteHeader).addChain(this.chainCase3).isChainTakeOverLastRun(true));
            this.chains = this.chains.append(chainStart);
            this.initialVars = this.initialVars.add("key", "default");
        }

        @Test
        public void finish() {
            this.initialVars = this.initialVars.add("skipChain1", "false").add("skipChain2", "false");
            this.resetTestRun();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).closeTestSuite();
            Mockito.doNothing().when(this.listener).setStepIndex(0);
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("name: ClickElement");
            Mockito.doReturn(true).when(this.step).run(Mockito.any());
            Mockito.doNothing().when(this.listener).endTest();
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2"), Mockito.any(InputData.class));
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=1");
            Mockito.doReturn(true).when(this.chainStep).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain: ClickElement key=2");
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.doReturn(true).when(this.chain2).run(Mockito.any());
            Mockito.doReturn(true)
                    .when(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase3"), Mockito.any(InputData.class));
            Mockito.doNothing().when(this.listener).startTest("chain3: ClickElement key=default");
            Mockito.doReturn(true).when(this.chain3).run(Mockito.any());

            assertTrue(this.target.finish());

            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2_0_chainCase_row_1"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2_0_chainCase_row_2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_0_suite2_1_chainCase2"), Mockito.any(InputData.class));
            Mockito.verify(this.listener)
                    .openTestSuite(Mockito.any(TestCase.class), Mockito.eq("suite_0_chainStart_1_chainCase3"), Mockito.any(InputData.class));
            Mockito.verify(this.listener).startTest("name: ClickElement");
            Mockito.verify(this.step).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain: ClickElement key=1");
            Mockito.verify(this.listener).startTest("chain: ClickElement key=2");
            Mockito.verify(this.chainStep, Mockito.times(2)).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain2: ClickElement key=default");
            Mockito.verify(this.chain2).run(Mockito.any());
            Mockito.verify(this.listener).startTest("chain3: ClickElement key=default");
            Mockito.verify(this.chain3, Mockito.times(1)).run(Mockito.any());
            Mockito.verify(this.listener, Mockito.times(5)).endTest();
            Mockito.verify(this.listener, Mockito.times(7)).closeTestSuite();
        }
    }

    public static class Quit extends AbstractTestRunTest {
        @Test
        public void quit() {
            final TestRunBuilder builder = new TestRunBuilder(this.head.map(it -> it.setChains(this.chains)));
            this.target = builder.createTestRun(this.log, this.driver, this.initialVars, this.listener);
            this.target.quit();
            Mockito.verify(this.log).debug("Quitting driver.");
            Mockito.verify(this.driver).quit();
        }

        @Test
        public void quit_withParentDriver() {
            this.head = this.head.builder().isShareState(true).build();
            this.resetTestRun();
            this.target.quit();
            Mockito.verify(this.driver, Mockito.never()).quit();
        }

    }
}
