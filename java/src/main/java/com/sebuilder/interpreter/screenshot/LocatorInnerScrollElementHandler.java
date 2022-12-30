package com.sebuilder.interpreter.screenshot;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;
import java.util.TreeMap;

public class LocatorInnerScrollElementHandler implements InnerScrollElementHandler {

    @Override
    public TreeMap<Integer, InnerElement> handleTarget(final Printable parent) {
        final TreeMap<Integer, InnerElement> innerPrintableElement = Maps.newTreeMap();
        this.handleScrollableTag(parent, innerPrintableElement, parent.getCtx());
        return innerPrintableElement;
    }

    public void handleScrollableTag(final Printable parent, final TreeMap<Integer, InnerElement> innerPrintableElement, final TestRun testRun) {
        if (!testRun.currentStep().locatorContains("locator")) {
            return;
        }
        final List<WebElement> elements = testRun.locator().findElements(testRun);
        elements.stream()
                .filter(element -> element.getTagName().equals("iframe"))
                .forEach(targetFrame -> {
                            final Frame printableFrame = this.toFrame(parent, targetFrame);
                            if (printableFrame.hasVerticalScroll() && printableFrame.getViewportHeight() > 0) {
                                innerPrintableElement.put(printableFrame.getPointY(), printableFrame);
                            }
                        }
                );
        elements.stream()
                .filter(element -> !element.getTagName().equals("iframe"))
                .filter(element -> {
                    final int scrollHeight = Integer.parseInt(element.getAttribute("scrollHeight"));
                    final int clientHeight = Integer.parseInt(element.getAttribute("clientHeight"));
                    final int scrollWidth = Integer.parseInt(element.getAttribute("scrollWidth"));
                    final int clientWidth = Integer.parseInt(element.getAttribute("clientWidth"));
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
                    final ScrollableTag tag = this.toScrollableTag(parent, testRun, targetDiv);
                    if (testRun.currentStep().locatorContains("locatorHeader")) {
                        final InnerElement headerArea = this.toScrollableTag(parent, testRun, testRun.locator("locatorHeader").find(testRun));
                        final InnerElement withHeader = new InnerElementWithHeader(tag, headerArea);
                        innerPrintableElement.put(withHeader.getPointY(), withHeader);
                    } else {
                        innerPrintableElement.put(tag.getPointY(), tag);
                    }
                });
    }

    public Frame toFrame(final Printable parent, final WebElement targetFrame) {
        final RemoteWebDriver wd = parent.driver();
        final ScrollableHeight height = Frame.getHeight(parent, wd, targetFrame);
        final ScrollableWidth width = Frame.getWidth(parent, wd, targetFrame);
        wd.switchTo().frame(targetFrame);
        final Frame result = new Frame(parent, targetFrame, this, height, width);
        wd.switchTo().parentFrame();
        return result;
    }

    public ScrollableTag toScrollableTag(final Printable parent, final TestRun testRun, final WebElement targetDiv) {
        final ScrollableHeight height = ScrollableTag.getHeight(testRun, targetDiv);
        final ScrollableWidth width = ScrollableTag.getWidth(testRun, targetDiv);
        return new ScrollableTag(parent, targetDiv, height, width);
    }

    private boolean isScrollable(final WebElement element, final String overflow) {
        return "auto".equals(overflow) || "scroll".equals(overflow) || ("visible".equals(overflow) && element.getTagName().equals("textarea"));
    }
}
