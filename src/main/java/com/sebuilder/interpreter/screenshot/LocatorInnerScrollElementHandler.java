package com.sebuilder.interpreter.screenshot;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.TreeMap;

public class LocatorInnerScrollElementHandler implements InnerScrollElementHandler {

    @Override
    public TreeMap<Integer, InnerElement> handleTarget(Printable parent) {
        TreeMap<Integer, InnerElement> innerPrintableElement = Maps.newTreeMap();
        this.handleScrollableTag(parent, innerPrintableElement, parent.getCtx());
        return innerPrintableElement;
    }

    public void handleScrollableTag(Printable parent, TreeMap<Integer, InnerElement> innerPrintableElement, TestRun testRun) {
        if (!testRun.currentStep().locatorContains("locator")) {
            return;
        }
        List<WebElement> elements = testRun.locator().findElements(testRun);
        elements.stream()
                .filter(element -> element.getTagName().equals("iframe"))
                .forEach(targetFrame -> {
                            Frame printableFrame = this.toFrame(parent, targetFrame);
                            if (printableFrame.hasVerticalScroll() && printableFrame.getViewportHeight() > 0) {
                                innerPrintableElement.put(printableFrame.getPointY(), printableFrame);
                            }
                        }
                );
        elements.stream()
                .filter(element -> !element.getTagName().equals("iframe"))
                .filter(element -> {
                    int scrollHeight = Integer.parseInt(element.getAttribute("scrollHeight"));
                    int clientHeight = Integer.parseInt(element.getAttribute("clientHeight"));
                    int scrollWidth = Integer.parseInt(element.getAttribute("scrollWidth"));
                    int clientWidth = Integer.parseInt(element.getAttribute("clientWidth"));
                    return (scrollHeight > clientHeight) || (scrollWidth > clientWidth);
                })
                .filter(element -> {
                    if (this.isScrollable(element, element.getCssValue("overflow"))) {
                        return true;
                    }
                    return this.isScrollable(element, element.getCssValue("overflow-y"))
                            || this.isScrollable(element, element.getCssValue("overflow-x"));
                })
                .forEach(targetDiv -> {
                    ScrollableTag tag = this.toScrollableTag(parent, testRun, targetDiv);
                    if (testRun.currentStep().locatorContains("locatorHeader")) {
                        InnerElement headerArea = this.toScrollableTag(parent, testRun, testRun.locator("locatorHeader").find(testRun));
                        InnerElement withHeader = new InnerElementWithHeader(tag, headerArea);
                        innerPrintableElement.put(withHeader.getPointY(), withHeader);
                    } else {
                        innerPrintableElement.put(tag.getPointY(), tag);
                    }
                });
    }

    public Frame toFrame(Printable parent, WebElement targetFrame) {
        RemoteWebDriver wd = parent.getWebDriver();
        ScrollableHeight height = Frame.getHeight(parent, wd, targetFrame);
        ScrollableWidth width = Frame.getWidth(parent, wd, targetFrame);
        wd.switchTo().frame(targetFrame);
        Frame result = new Frame(parent, targetFrame, this, height, width);
        wd.switchTo().parentFrame();
        return result;
    }

    public ScrollableTag toScrollableTag(Printable parent, TestRun testRun, WebElement targetDiv) {
        ScrollableHeight height = ScrollableTag.getHeight(testRun, targetDiv);
        ScrollableWidth width = ScrollableTag.getWidth(testRun, targetDiv);
        return new ScrollableTag(parent, targetDiv, height, width);
    }

    private boolean isScrollable(WebElement element, String overflow) {
        return "auto".equals(overflow) || "scroll".equals(overflow) || ("visible".equals(overflow) && element.getTagName().equals("textarea"));
    }
}
