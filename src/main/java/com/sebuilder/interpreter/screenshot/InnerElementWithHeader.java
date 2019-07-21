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

    public InnerElementWithHeader(InnerElement element, InnerElement headerElement) {
        this.element = element;
        this.headerElement = new ScrollableTag(headerElement.getParent()
                , element.getElement()
                , headerElement.getPointY()
                , headerElement.getScrollableHeight()
                , headerElement.getViewportHeight()
                , element.getPointX()
                , element.getScrollableWidth()
                , element.getViewportWidth());
    }

    @Override
    public RemoteWebDriver getWebDriver() {
        return this.element.getWebDriver();
    }

    @Override
    public TestRun getCtx() {
        return this.element.getCtx();
    }

    @Override
    public BufferedImage printImage(VerticalPrinter aPrinter, int fromPointY) {
        int imageHeight = this.getFullImageHeight();
        int imageWidth = this.getFullImageWidth();
        BufferedImage result = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = result.createGraphics();
        BufferedImage headerImage = this.headerElement.printImage(new HorizontalPrinter());
        headerImage = headerImage.getSubimage(0
                , element.convertImageHeight(fromPointY)
                , headerImage.getWidth()
                , element.convertImageHeight(this.element.getPointY() - this.headerElement.getPointY()));
        graphics.drawImage(headerImage, 0, 0, null);
        graphics.drawImage(this.element.printImage(aPrinter
                , element.convertDocumentHeight(headerImage.getHeight()))
                , 0
                , headerImage.getHeight()
                , null);
        graphics.dispose();
        return result;
    }

    @Override
    public BufferedImage printImage(HorizontalPrinter aPrinter) {
        return element.printImage(aPrinter);
    }

    @Override
    public Map<Integer, InnerElement> getInnerScrollableElement() {
        return element.getInnerScrollableElement();
    }

    @Override
    public WebElement getElement() {
        return element.getElement();
    }

    @Override
    public int getPointY() {
        return this.headerElement.getPointY();
    }

    @Override
    public Printable getParent() {
        return element.getParent();
    }

    @Override
    public int getViewportHeight() {
        return element.getViewportHeight() + this.element.getPointY() - this.headerElement.getPointY();
    }

    @Override
    public int getScrollableHeight() {
        return element.getScrollableHeight() + this.element.getPointY() - this.headerElement.getPointY();
    }

    @Override
    public int getInnerScrollHeight() {
        return element.getInnerScrollHeight();
    }

    @Override
    public int getScrollHeight() {
        return element.getScrollHeight();
    }

    @Override
    public int getFullImageHeight() {
        return element.getFullImageHeight() + this.convertImageHeight(this.element.getPointY() - this.headerElement.getPointY());
    }

    @Override
    public boolean hasVerticalScroll() {
        return element.hasVerticalScroll();
    }

    @Override
    public boolean isMoveScrollTopTo(int aPointY) {
        return element.isMoveScrollTopTo(aPointY);
    }

    @Override
    public Map<Integer, InnerElement> getInnerVerticalScrollableElement() {
        return element.getInnerVerticalScrollableElement();
    }

    @Override
    public void scrollVertically(int scrollY) {
        element.scrollVertically(scrollY);
    }

    @Override
    public void scrollVertically(int scrollY, WebElement element) {
        this.element.scrollVertically(scrollY, element);
    }

    @Override
    public int scrollOutVertically(int printedHeight, int scrolledHeight) {
        return element.scrollOutVertically(printedHeight, scrolledHeight);
    }

    @Override
    public int nextPrintableHeight(int remainViewPortHeight, int printedHeight) {
        return element.nextPrintableHeight(remainViewPortHeight, printedHeight);
    }

    @Override
    public int convertImageHeight(int documentHeight) {
        return element.convertImageHeight(documentHeight);
    }

    @Override
    public int convertDocumentHeight(int imageHeight) {
        return element.convertDocumentHeight(imageHeight);
    }

    @Override
    public int getImageHeight() {
        return element.getImageHeight();
    }

    @Override
    public int getImageWidth() {
        return element.getImageWidth();
    }

    @Override
    public int getWindowHeight() {
        return element.getWindowHeight();
    }

    @Override
    public int getWindowWidth() {
        return element.getWindowWidth();
    }

    @Override
    public int getFullHeight() {
        return element.getFullHeight();
    }

    @Override
    public int getFullWidth() {
        return element.getFullWidth();
    }

    @Override
    public BufferedImage getScreenshot() {
        return element.getScreenshot();
    }

    @Override
    public long scrollTimeout() {
        return element.scrollTimeout();
    }

    @Override
    public void waitForScrolling() {
        element.waitForScrolling();
    }

    @Override
    public int getPointX() {
        return element.getPointX();
    }

    @Override
    public int getViewportWidth() {
        return element.getViewportWidth();
    }

    @Override
    public int getScrollableWidth() {
        return element.getScrollableWidth();
    }

    @Override
    public int getInnerScrollWidth() {
        return element.getInnerScrollWidth();
    }

    @Override
    public int getScrollWidth() {
        return element.getScrollWidth();
    }

    @Override
    public int getFullImageWidth() {
        return element.getFullImageWidth();
    }

    @Override
    public boolean hasHorizontalScroll() {
        return element.hasHorizontalScroll();
    }

    @Override
    public boolean isMoveScrollLeftTo(int aPointX) {
        return element.isMoveScrollLeftTo(aPointX);
    }

    @Override
    public Map<Integer, InnerElement> getInnerHorizontalScrollableElement() {
        return element.getInnerHorizontalScrollableElement();
    }

    @Override
    public void scrollHorizontally(int scrollX) {
        element.scrollHorizontally(scrollX);
    }

    @Override
    public void scrollHorizontally(int scrollX, WebElement element) {
        this.element.scrollHorizontally(scrollX, element);
    }

    @Override
    public int scrollOutHorizontally(int printedWidth) {
        return element.scrollOutHorizontally(printedWidth);
    }

    @Override
    public int convertImageWidth(int documentWidth) {
        return element.convertImageWidth(documentWidth);
    }

    @Override
    public int convertDocumentWidth(int imageWidth) {
        return element.convertDocumentWidth(imageWidth);
    }
}
