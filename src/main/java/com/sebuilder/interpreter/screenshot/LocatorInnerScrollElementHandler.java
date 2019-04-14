package com.sebuilder.interpreter.screenshot;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LocatorInnerScrollElementHandler implements InnerScrollElementHandler {

    private final RemoteWebDriver driver;

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
            int border = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-top-width'));", targetFrame)).intValue();
            int pointY = getPointY(targetFrame, border, wd);
            int viewportHeight = getClientHeight(targetFrame, border, wd);
            int borderWidth = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-left-width'));", targetFrame)).intValue();
            int pointX = targetFrame.getLocation().getX() + borderWidth;
            int viewportWidth = Integer.parseInt(targetFrame.getAttribute("clientWidth"));
            wd.switchTo().frame(targetFrame);
            int scrollableHeight = getScrollHeight(parent.getFullHeight(), border, wd);
            int scrollableWidth = parent.getFullWidth();
            Frame printableFrame = new Frame(parent
                    , targetFrame
                    , this
                    , pointY
                    , scrollableHeight
                    , viewportHeight
                    , pointX
                    , scrollableWidth
                    , viewportWidth);
            if (printableFrame.hasVerticalScroll() && printableFrame.getViewportHeight() > 0) {
                innerPrintableElement.put(printableFrame.getPointY(), printableFrame);
            }
            wd.switchTo().parentFrame();
        }
    }

    public void handleScrollableTag(Printable parent, TreeMap<Integer, InnerElement> innerPrintableElement, TestRun testRun) {
        if (!testRun.currentStep().locatorContains("locator")) {
            return;
        }
        List<WebElement> divs = testRun.locator().findElements(testRun)
                .stream()
                .filter(element -> {
                    int scrollHeight = Integer.valueOf(element.getAttribute("scrollHeight"));
                    int clientHeight = Integer.valueOf(element.getAttribute("clientHeight"));
                    int scrollWidth = Integer.valueOf(element.getAttribute("scrollWidth"));
                    int clientWidth = Integer.valueOf(element.getAttribute("clientWidth"));
                    return (scrollHeight > clientHeight) || (scrollWidth > clientWidth);
                })
                .filter(element -> {
                    if (isScrollable(element, element.getCssValue("overflow"))) {
                        return true;
                    }
                    return isScrollable(element, element.getCssValue("overflow-y"))
                            || isScrollable(element, element.getCssValue("overflow-x"));
                })
                .collect(Collectors.toList());

        for (WebElement targetDiv : divs) {
            Point framePoint = targetDiv.getLocation();

            int height = new BigDecimal(targetDiv.getCssValue("height").replaceAll("[^0-9\\.]", "")).intValue();
            int clientHeight = Integer.valueOf(targetDiv.getAttribute("clientHeight"));
            int scrollableDivHeight = Integer.valueOf(targetDiv.getAttribute("scrollHeight"));

            int width = new BigDecimal(targetDiv.getCssValue("width").replaceAll("[^0-9\\.]", "")).intValue();
            int clientWidth = Integer.valueOf(targetDiv.getAttribute("clientWidth"));
            int scrollableDivWidth = Integer.valueOf(targetDiv.getAttribute("scrollWidth"));

            int borderHeight = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-top-width'));", targetDiv)).intValue();
            int pointY = framePoint.getY() + borderHeight;
            int borderWidth = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-left-width'));", targetDiv)).intValue();
            int pointX = framePoint.getX() + borderWidth;
            if (Objects.equal(targetDiv.getCssValue("box-sizing"), "border-box")) {
                height = height - borderHeight * 2;
                width = width - borderWidth * 2;
            } else {
                int paddingTop = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-top') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
                pointY = pointY + paddingTop;
                int paddingLeft = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-left') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
                pointX = pointX + paddingLeft;
            }
            if (testRun.driver() instanceof FirefoxDriver) {
                int paddingBottom = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-bottom') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
                height = height - paddingBottom;
                int paddingRight = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-right') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
                width = width - paddingRight;
            }
            scrollableDivHeight = scrollableDivHeight - (clientHeight - height);
            scrollableDivWidth = scrollableDivWidth - (clientWidth - width);

            ScrollableTag tag = new ScrollableTag(parent
                    , targetDiv
                    , pointY
                    , scrollableDivHeight
                    , height
                    , pointX
                    , scrollableDivWidth
                    , width
            );
            innerPrintableElement.put(tag.getPointY(), tag);
        }
    }

    protected int getPointY(WebElement targetFrame, int border, RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return targetFrame.getLocation().getY() + border * 2;
        }
        return targetFrame.getLocation().getY() + border;
    }

    protected int getClientHeight(WebElement targetFrame, int border, RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return Integer.parseInt(targetFrame.getAttribute("clientHeight")) - border;
        }
        return Integer.parseInt(targetFrame.getAttribute("clientHeight"));
    }

    protected int getScrollHeight(int scrollHeight, int border, RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return scrollHeight - border;
        }
        return scrollHeight;
    }

    private boolean isScrollable(WebElement element, String overflow) {
        return "auto".equals(overflow) || "scroll".equals(overflow) || ("visible".equals(overflow) && element.getTagName().equals("textarea"));
    }
}
