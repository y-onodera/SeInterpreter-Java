package com.sebuilder.interpreter.screenshot;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class InnerElementScrollStrategy implements VerticalSurvey {
    private final RemoteWebDriver driver;

    @Override
    public RemoteWebDriver getWebDriver() {
        return driver;
    }

    public InnerElementScrollStrategy(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public TreeMap<Integer, InnerElement> printTarget(Printable parent) {
        TreeMap<Integer, InnerElement> innerPrintableElement = Maps.newTreeMap();
        handleFrame(parent, innerPrintableElement);
        handleScrollableTag(parent, innerPrintableElement, parent.getCtx());
        return innerPrintableElement;
    }

    public void handleFrame(Printable parent, TreeMap<Integer, InnerElement> innerPrintableElement) {
        RemoteWebDriver wd = getWebDriver();
        List<WebElement> frames = wd.findElementsByTagName("iframe");
        for (WebElement targetFrame : frames) {
            int pointY = targetFrame.getLocation().getY();
            int viewportHeight = Integer.parseInt(targetFrame.getAttribute("clientHeight"));
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
                    String overflow = element.getCssValue("overflow");
                    return "auto".equals(overflow) || "scroll".equals(overflow) || ("visible".equals(overflow) && element.getTagName().equals("textarea"));
                })
                .collect(Collectors.toList());
        for (WebElement targetDiv : divs) {
            int divViewportHeight = Integer.parseInt(targetDiv.getAttribute("clientHeight"));
            Point framePoint = targetDiv.getLocation();
            int pointY = framePoint.getY();
            int scrollableDivHeight = Integer.valueOf(targetDiv.getAttribute("scrollHeight"));
            innerPrintableElement.put(pointY, new ScrollableTag(parent, pointY, targetDiv, scrollableDivHeight, divViewportHeight));
        }
    }
}
