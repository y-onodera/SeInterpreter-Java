package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class InnerElementWithHeader implements InnerElement {
    private final InnerElement element;
    private final InnerElement headerElement;
    private final ScrollableWidth width;
    private final ScrollableHeight height;

    public InnerElementWithHeader(final InnerElement element, final InnerElement headerElement) {
        this.element = element;
        this.headerElement = new ScrollableTag(headerElement.getParent()
                , element.getElement()
                , headerElement.getHeight()
                , element.getWidth());
        this.width = element.getWidth();
        this.height = new Height(element, headerElement);
    }

    @Override
    public ScrollableWidth getWidth() {
        return this.width;
    }

    @Override
    public ScrollableHeight getHeight() {
        return this.height;
    }

    @Override
    public RemoteWebDriver driver() {
        return this.element.driver();
    }

    @Override
    public TestRun getCtx() {
        return this.element.getCtx();
    }

    @Override
    public BufferedImage printImage(final VerticalPrinter aPrinter, final int fromPointY) {
        final int imageHeight = this.getFullImageHeight();
        final int imageWidth = this.getFullImageWidth();
        final BufferedImage result = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D graphics = result.createGraphics();
        BufferedImage headerImage = this.headerElement.printImage(new HorizontalPrinter());
        headerImage = headerImage.getSubimage(0
                , this.element.convertImageHeight(fromPointY)
                , headerImage.getWidth()
                , this.element.convertImageHeight(this.element.getPointY() - this.headerElement.getPointY()));
        graphics.drawImage(headerImage, 0, 0, null);
        final int mainImageStartFrom = this.element.convertDocumentHeight(headerImage.getHeight()) + fromPointY;
        graphics.drawImage(this.element.printImage(aPrinter
                        , mainImageStartFrom)
                , 0
                , headerImage.getHeight()
                , null);
        graphics.dispose();
        return result;
    }

    @Override
    public BufferedImage printImage(final HorizontalPrinter aPrinter) {
        return this.element.printImage(aPrinter);
    }

    @Override
    public Map<Integer, InnerElement> getInnerScrollableElement() {
        return this.element.getInnerScrollableElement();
    }

    @Override
    public WebElement getElement() {
        return this.element.getElement();
    }

    @Override
    public Printable getParent() {
        return this.element.getParent();
    }

    @Override
    public int getInnerScrollHeight() {
        return this.element.getInnerScrollHeight();
    }

    @Override
    public int getFullImageHeight() {
        return this.element.getFullImageHeight() + this.convertImageHeight(this.element.getPointY() - this.headerElement.getPointY());
    }

    @Override
    public Map<Integer, InnerElement> getInnerVerticalScrollableElement() {
        return this.element.getInnerVerticalScrollableElement();
    }

    @Override
    public int nextPrintableHeight(final int remainViewPortHeight, final int printedHeight) {
        return this.element.nextPrintableHeight(remainViewPortHeight, printedHeight);
    }

    @Override
    public int convertImageHeight(final int documentHeight) {
        return this.element.convertImageHeight(documentHeight);
    }

    @Override
    public int convertDocumentHeight(final int imageHeight) {
        return this.element.convertDocumentHeight(imageHeight);
    }

    @Override
    public int getImageHeight() {
        return this.element.getImageHeight();
    }

    @Override
    public int getImageWidth() {
        return this.element.getImageWidth();
    }

    @Override
    public int getWindowHeight() {
        return this.element.getWindowHeight();
    }

    @Override
    public int getWindowWidth() {
        return this.element.getWindowWidth();
    }

    @Override
    public int getFullHeight() {
        return this.element.getFullHeight();
    }

    @Override
    public int getFullWidth() {
        return this.element.getFullWidth();
    }

    @Override
    public BufferedImage getScreenshot() {
        return this.element.getScreenshot();
    }

    @Override
    public int getInnerScrollWidth() {
        return this.element.getInnerScrollWidth();
    }

    @Override
    public int getFullImageWidth() {
        return this.element.getFullImageWidth();
    }

    @Override
    public Map<Integer, InnerElement> getInnerHorizontalScrollableElement() {
        return this.element.getInnerHorizontalScrollableElement();
    }

    @Override
    public int convertImageWidth(final int documentWidth) {
        return this.element.convertImageWidth(documentWidth);
    }

    @Override
    public int convertDocumentWidth(final int imageWidth) {
        return this.element.convertDocumentWidth(imageWidth);
    }

    private static class Height extends ScrollableHeight {
        private final InnerElement element;

        public Height(final InnerElement element, final InnerElement headerElement) {
            super(new ScrollableHeight.Builder()
                    .setWebDriver(element.driver())
                    .setPointY(headerElement.getPointY())
                    .setViewportHeight(element.getViewportHeight() + element.getPointY() - headerElement.getPointY())
                    .setScrollableHeight(element.getScrollableHeight() + element.getPointY() - headerElement.getPointY())
            );
            this.element = element;
        }

        @Override
        public int getScrollHeight() {
            return this.element.getScrollHeight();
        }

        @Override
        public boolean hasVerticalScroll() {
            return this.element.hasVerticalScroll();
        }

        @Override
        public boolean isEnableMoveScrollTopTo(final int aPointY) {
            return this.element.isEnableMoveScrollTopTo(aPointY);
        }

        @Override
        public void scrollVertically(final int scrollY) {
            this.element.scrollVertically(scrollY);
        }

        @Override
        public int scrollOutVertically(final int printedHeight, final int scrolledHeight) {
            return this.element.scrollOutVertically(printedHeight, scrolledHeight);
        }
    }
}
