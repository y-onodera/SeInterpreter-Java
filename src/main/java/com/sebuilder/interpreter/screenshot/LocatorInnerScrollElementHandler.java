package com.sebuilder.interpreter.screenshot;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LocatorInnerScrollElementHandler implements VerticalSurvey, InnerScrollElementHandler {
    private final RemoteWebDriver driver;

    @Override
    public RemoteWebDriver getWebDriver() {
        return driver;
    }

    public LocatorInnerScrollElementHandler(RemoteWebDriver driver) {
        this.driver = driver;
    }

    @Override
    public TreeMap<Integer, InnerElement> handleTarget(Printable parent) {
        TreeMap<Integer, InnerElement> innerPrintableElement = Maps.newTreeMap();
        handleFrame(parent, innerPrintableElement);
        handleScrollableTag(parent, innerPrintableElement, parent.getCtx());
        return innerPrintableElement;
    }

    public void handleFrame(Printable parent, TreeMap<Integer, InnerElement> innerPrintableElement) {
        RemoteWebDriver wd = getWebDriver();
        List<WebElement> frames = wd.findElementsByTagName("iframe");
        for (WebElement targetFrame : frames) {
            int viewportHeight = Integer.parseInt(targetFrame.getAttribute("clientHeight"));
            int border = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-top-width'));", targetFrame)).intValue();
            int pointY = targetFrame.getLocation().getY() + border;
            wd.switchTo().frame(targetFrame);
            int scrollableHeight = this.getFullHeight();
            Frame printableFrame = new Frame(parent, pointY, targetFrame, scrollableHeight, viewportHeight, this);
            if (printableFrame.hasScroll() && printableFrame.getViewportHeight() > 0) {
                innerPrintableElement.put(printableFrame.getPointY(), printableFrame);
            }
            wd.switchTo().parentFrame();
        }
    }

    public void handleScrollableTag(Printable parent, TreeMap<Integer, InnerElement> innerPrintableElement, TestRun testRun) {
        if (!testRun.currentStep().locatorParams.containsKey("locator")) {
            return;
        }
        List<WebElement> divs = testRun.locator().findElements(testRun)
                .stream()
                .filter(element -> {
                    int scrollHeight = Integer.valueOf(element.getAttribute("scrollHeight"));
                    int clientHeight = Integer.valueOf(element.getAttribute("clientHeight"));
                    return scrollHeight > clientHeight;
                })
                .filter(element -> {
                    if (isScrollable(element, element.getCssValue("overflow"))) {
                        return true;
                    }
                    return isScrollable(element, element.getCssValue("overflow-y"));
                })
                .collect(Collectors.toList());
        for (WebElement targetDiv : divs) {
            int clientHeight = Integer.valueOf(targetDiv.getAttribute("clientHeight"));
            int border = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-top-width'));", targetDiv)).intValue();
            int paddingTop = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-top') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
            int paddingBottom = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-bottom') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
            int height = clientHeight - paddingTop - paddingBottom - border * 2;
            Point framePoint = targetDiv.getLocation();
            int pointY = framePoint.getY() + paddingTop + border;
            int scrollableDivHeight = Integer.valueOf(targetDiv.getAttribute("scrollHeight")) - paddingTop - paddingBottom - border * 2;
            ScrollableTag tag = new ScrollableTag(parent, pointY, targetDiv, scrollableDivHeight, height);
            innerPrintableElement.put(tag.getPointY(), tag);
        }
    }

    private boolean isScrollable(WebElement element, String overflow) {
        return "auto".equals(overflow) || "scroll".equals(overflow) || ("visible".equals(overflow) && element.getTagName().equals("textarea"));
    }
}
