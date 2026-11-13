package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.step.StaleElementRetry;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StaleElementGetterTest {
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

    private TestRun createTestRun(final AbstractGetter getter) {
        return this.createTestRun(new StepBuilder(getter.toAssert()));
    }

    private TestRun createWaitForRun(final AbstractGetter getter, final boolean negated) {
        return this.createTestRun(new StepBuilder(getter.toWaitFor())
                .negated(negated)
                .put("maxWait", "1000")
                .put("interval", "10"));
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

    @Test
    public void elementPresentRetriesWhenFindElementsThrowsStale() {
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenThrow(new StaleElementReferenceException("stale"))
                .thenReturn(List.of(this.element));
        assertEquals("true", new ElementPresent().get(this.createTestRun(new ElementPresent())));
    }

    @Test
    public void elementVisibleLooksUpAgainWhenElementGoesStale() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        assertEquals("true", new ElementVisible().get(this.createTestRun(new ElementVisible())));
    }

    @Test
    public void elementEnableLooksUpAgainWhenElementGoesStale() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isEnabled();
        Mockito.doReturn(false).when(this.element).isEnabled();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        assertEquals("false", new ElementEnable().get(this.createTestRun(new ElementEnable())));
    }

    @Test
    public void staleOnEveryAttemptIsRethrown() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target"))).thenReturn(List.of(this.staleElement));
        final TestRun ctx = this.createTestRun(new ElementVisible());
        assertThrows(StaleElementReferenceException.class, () -> new ElementVisible().get(ctx));
        Mockito.verify(this.driver, Mockito.times(StaleElementRetry.RETRY_COUNT)).findElements(By.id("target"));
    }

    @Test
    public void textLooksUpAgainWhenElementGoesStale() {
        Mockito.doReturn(true).when(this.staleElement).isDisplayed();
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).getText();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.doReturn("hello").when(this.element).getText();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        assertEquals("hello", new Text().get(this.createTestRun(new Text())));
    }

    @Test
    public void waitForKeepsWaitingWhileElementIsStale() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement), List.of(this.staleElement), List.of(this.staleElement),
                        List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        final TestRun ctx = this.createWaitForRun(new ElementVisible(), false);
        assertTrue(ctx.currentStep().type().run(ctx));
    }

    @Test
    public void negatedWaitForDoesNotPassOnStale() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement), List.of(this.staleElement), List.of(this.staleElement))
                .thenReturn(List.of(this.element));
        final TestRun ctx = this.createWaitForRun(new ElementVisible(), true);
        assertFalse(ctx.currentStep().type().run(ctx));
    }

    @Test
    public void waitForRethrowsStaleAtMaxWait() {
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.when(this.driver.findElements(By.id("target"))).thenReturn(List.of(this.staleElement));
        final TestRun ctx = this.createWaitForRun(new ElementVisible(), false);
        final long start = System.currentTimeMillis();
        assertThrows(StaleElementReferenceException.class, () -> ctx.currentStep().type().run(ctx));
        assertTrue(System.currentTimeMillis() - start >= 1000);
    }
}
