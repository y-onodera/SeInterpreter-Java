package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface Printable extends VerticalSurvey {

    default void printImage(int fromPointY) {
        this.setUpPrint(fromPointY);
        int viewportPrintFrom = fromPointY;
        int remainViewPortHeight = this.getViewportHeight();
        while (nextPrintableHeight(remainViewPortHeight) > 0) {
            int viewportPrinted = this.printInnerScrollElement(fromPointY);
            if (viewportPrinted > 0) {
                remainViewPortHeight = nextPrintableHeight(fromPointY + getViewportHeight() - viewportPrinted);
                viewportPrintFrom = viewportPrinted;
            }
            if (remainViewPortHeight > 0) {
                BufferedImage part = this.getScreenshot(viewportPrintFrom, remainViewPortHeight, this.getViewportHeight());
                int notScrolled = this.appendImageAndScroll(part);
                if (notScrolled == 0) {
                    viewportPrintFrom = fromPointY;
                    remainViewPortHeight = this.getViewportHeight();
                } else {
                    remainViewPortHeight = nextPrintableHeight(remainViewPortHeight);
                    viewportPrintFrom = fromPointY + this.getViewportHeight() - remainViewPortHeight;
                }
            }
        }
    }

    void setUpPrint(int fromPointY);

    int nextPrintableHeight(int remainViewPortHeight);

    int printInnerScrollElement(int printFrom);

    default BufferedImage getScreenshot(int printFrom, int height) {
        return this.getScreenshot(printFrom, height, height);
    }

    default BufferedImage getScreenshot(int printFrom, int remainHeight, int viewportHeight) {
        BufferedImage part = this.getScreenshot();
        int height = Math.min(part.getHeight(), viewportHeight);
        int width = Math.min(part.getWidth(), this.getWindowWidth());
        if (remainHeight < height) {
            if (printFrom + viewportHeight < part.getHeight()) {
                part = part.getSubimage(0, printFrom, width, remainHeight);
            } else {
                if (printFrom + remainHeight < part.getHeight()) {
                    part = part.getSubimage(0, printFrom, width, remainHeight);
                } else if (printFrom < part.getHeight()) {
                    part = part.getSubimage(0, printFrom, width, part.getHeight() - printFrom);
                }
            }
        } else {
            if (printFrom + height < part.getHeight()) {
                part = part.getSubimage(0, printFrom, width, height);
            } else if (printFrom < part.getHeight()) {
                part = part.getSubimage(0, printFrom, width, part.getHeight() - printFrom);
            }
        }
        return part;
    }

    default BufferedImage getScreenshot() {
        try (ByteArrayInputStream imageArrayStream = new ByteArrayInputStream(getWebDriver().getScreenshotAs(OutputType.BYTES))) {
            return ImageIO.read(imageArrayStream);
        } catch (IOException var9) {
            throw new RuntimeException("Can not parse screenshot data", var9);
        }
    }

    default int appendImageAndScroll(BufferedImage part) {
        this.appendImage(part);
        return this.scrollOutPrintedPart(part.getHeight());
    }

    void appendImage(BufferedImage part);

    int scrollOutPrintedPart(int printedHeight);

    @Override
    default RemoteWebDriver getWebDriver() {
        return getCtx().driver();
    }

    TestRun getCtx();

    default boolean hasScroll() {
        return this.getScrollableHeight() > this.getViewportHeight();
    }

    long scrollTimeout();

    int getViewportHeight();

    int getScrollableHeight();

    int getInnerScrollHeight();

    default int getScrollHeight() {
        return getScrollableHeight() - getViewportHeight() + getInnerScrollHeight();
    }

    default int getWindowWidth() {
        return ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return window.innerWidth || document.documentElement.clientWidth || document.getElementsByTagName('body')[0].clientWidth;", new Object[0])).intValue();
    }

    default void scrollVertically(int scrollY) {
        if (this.hasScroll()) {
            JavascriptExecutor.class.cast(getWebDriver()).executeScript("scrollTo(0, arguments[0]); return [];", scrollY);
            waitForScrolling();
        }
    }

    default void waitForScrolling() {
        try {
            Thread.sleep(this.scrollTimeout());
        } catch (InterruptedException var2) {
            throw new IllegalStateException("Exception while waiting for scrolling", var2);
        }
    }

    int getPrintedHeight();

    void setPrintedHeight(int printedHeight);
}
