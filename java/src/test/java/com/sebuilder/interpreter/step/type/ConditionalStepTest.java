package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.*;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class ConditionalStepTest {
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

    private TestRun target;

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
        Mockito.doThrow(new StaleElementReferenceException("stale")).when(this.staleElement).isDisplayed();
        Mockito.doReturn(true).when(this.element).isDisplayed();
        Mockito.doReturn(true).when(this.element).isEnabled();
        final TestCase head = new TestCaseBuilder()
                .addStep(new StepBuilder(new ClickElement())
                        .put("locator", new Locator("id", "target"))
                        .put("maxWait", "1000")
                        .put("interval", "10")
                        .build())
                .build();
        this.target = new TestRunBuilder(head)
                .createTestRun(this.log, this.driver, new InputData(), this.listener);
        this.target.toNextStepIndex();
    }

    @Test
    public void staleDuringReadyCheckKeepsWaiting() {
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.staleElement))
                .thenReturn(List.of(this.element));

        assertTrue(new ClickElement().run(this.target));
        Mockito.verify(this.element).click();
    }

    @Test
    public void elementRemovedBeforeActionIsRetried() {
        // WaitFor evaluates visible+enable twice when ready (loop condition and final result),
        // so the first four lookups belong to the readiness check and the fifth to doRun().
        Mockito.when(this.driver.findElements(By.id("target")))
                .thenReturn(List.of(this.element), List.of(this.element), List.of(this.element), List.of(this.element))
                .thenReturn(List.of())
                .thenReturn(List.of(this.element));

        assertTrue(new ClickElement().run(this.target));
        Mockito.verify(this.element).click();
    }

    @Test
    public void findThrowsNoSuchElementWhenNothingMatches() {
        Mockito.when(this.driver.findElements(By.id("target"))).thenReturn(List.of());

        final NoSuchElementException actual = assertThrows(NoSuchElementException.class
                , () -> new Locator("id", "target").find(this.driver));
        assertTrue(actual.getMessage().startsWith("no such element: id:target"));
    }
}
