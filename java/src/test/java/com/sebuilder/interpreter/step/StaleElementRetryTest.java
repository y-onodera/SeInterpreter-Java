package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.step.getter.ElementEnable;
import com.sebuilder.interpreter.step.getter.ElementPresent;
import com.sebuilder.interpreter.step.getter.ElementVisible;
import com.sebuilder.interpreter.step.getter.Text;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class StaleElementRetryTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    RemoteWebDriver driver;
    @Mock
    Logger log;
    @Mock
    TestRunListener listener;
    @Mock
    WebElement staleElement;
    @Mock
    WebElement element;

    @Before
    public void setUp() {
        Context.getInstance().setBrowser("Chrome");
        Context.getInstance().setDataSourceDirectory("datasource");
        Context.getInstance().setDataSourceEncoding("UTF-8");
        Context.getInstance().setAspect(new Aspect());
        Mockito.doReturn(new File(".", "result")).when(this.listener).getResultDir();
        Mockito.doReturn(new File(".", "screenshot")).when(this.listener).getScreenShotOutputDirectory();
        Mockito.doReturn(new File(".", "template")).when(this.listener).getTemplateOutputDirectory();
        Mockito.doReturn(new File(".", "download")).when(this.listener).getDownloadDirectory();
    }

    private TestRun createTestRun(final StepBuilder stepBuilder) {
        final TestCase head = new TestCaseBuilder()
                .addStep(stepBuilder
                        .put("locator", new Locator("id", "target"))
                        .build())
                .build();
        final TestRun result = new TestRunBuilder(head)
                .createTestRun(this.log, this.driver, new InputData(), this.listener);
        result.toNextStepIndex();
        return result;
    }

    private TestRun createWaitForRun(final Getter getter, final boolean negated) {
        return this.createTestRun(new StepBuilder(getter.toWaitFor())
                .negated(negated)
                .put("maxWait", "200")
                .put("interval", "10"));
    }

    private boolean run(final TestRun ctx) {
        return ctx.currentStep().run(ctx);
    }

    @Test
    public void getterRetriesWhenFindElementsThrowsStale() {
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenThrow(new StaleElementReferenceException("stale"))
                .thenReturn(List.of(this.element));
        final Getter getter = new ElementPresent();
        assertTrue(getter.test(this.createTestRun(new StepBuilder(getter.toAssert()))));
    }

    @Test
    public void getterLooksUpAgainWhenElementGoesStale() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isEnabled();
        Mockito.doReturn(true).when(this.element).isEnabled();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        final Getter getter = new ElementEnable();
        assertTrue(getter.test(this.createTestRun(new StepBuilder(getter.toAssert()))));
    }

    @Test
    public void staleOnEveryAttemptIsRethrown() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target"))).thenReturn(List.of(this.staleElement));
        final Getter getter = new ElementVisible();
        final TestRun ctx = this.createTestRun(new StepBuilder(getter.toAssert()));
        assertThrows(StaleElementReferenceException.class, () -> getter.test(ctx));
        Mockito.verify(this.driver, Mockito.times(StaleElementRetry.RETRY_COUNT)).findElements(By.id("target"));
    }

    @Test
    public void storeLooksUpAgainWhenElementGoesStale() {
        Mockito.doReturn(true).when(this.staleElement).isDisplayed();
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).getText();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.doReturn("hello").when(this.element).getText();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        final TestRun ctx = this.createTestRun(new StepBuilder(new Text().toStore()).put("variable", "got"));
        assertTrue(this.run(ctx));
        assertEquals("hello", ctx.vars().get("got"));
    }

    @Test
    public void waitForKeepsWaitingWhileElementIsStale() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement), List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        assertTrue(this.run(this.createWaitForRun(new ElementVisible(), false)));
    }

    @Test
    public void negatedWaitForDoesNotPassOnStale() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        assertFalse(this.run(this.createWaitForRun(new ElementVisible(), true)));
    }

    @Test
    public void waitForRethrowsStaleAtMaxWait() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target"))).thenReturn(List.of(this.staleElement));
        final TestRun ctx = this.createWaitForRun(new ElementVisible(), false);
        assertThrows(StaleElementReferenceException.class, () -> this.run(ctx));
    }

    @Test
    public void stepRunRetriesStepTypeThatAllowsIt() {
        final FlakyStep step = new FlakyStep(true);
        assertTrue(this.run(this.createTestRun(new StepBuilder(step))));
        assertEquals(2, step.calls);
    }

    @Test
    public void stepRunDoesNotRetryOtherStepTypes() {
        final FlakyStep step = new FlakyStep(false);
        final TestRun ctx = this.createTestRun(new StepBuilder(step));
        assertThrows(StaleElementReferenceException.class, () -> this.run(ctx));
        assertEquals(1, step.calls);
    }

    private static class FlakyStep extends AbstractStepType {
        private final boolean retryOnStale;
        private int calls;

        FlakyStep(final boolean retryOnStale) {
            this.retryOnStale = retryOnStale;
        }

        @Override
        public boolean run(final TestRun ctx) {
            if (this.calls++ == 0) {
                throw new StaleElementReferenceException("stale");
            }
            return true;
        }

        @Override
        public boolean isRetryOnStale() {
            return this.retryOnStale;
        }
    }
}
