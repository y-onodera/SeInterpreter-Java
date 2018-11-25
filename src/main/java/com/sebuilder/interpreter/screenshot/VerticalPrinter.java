package com.sebuilder.interpreter.screenshot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.stream.Collectors;

public class VerticalPrinter {

    private int printedHeight;
    private int printedImageHeight;
    private int scrolledHeight;
    private BufferedImage finalImage;
    private Graphics2D graphics;

    public BufferedImage getImage(Printable target, int fromPointY) {
        try {
            int imageHeight = target.getScrollableHeight() + target.getInnerScrollHeight();
            int imageWidth = target.getWindowWidth() + target.getScrollWidth() + target.getInnerScrollWidth();
            this.finalImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
            this.graphics = this.finalImage.createGraphics();
            this.printImage(target, fromPointY);
        } finally {
            this.graphics.dispose();
            return finalImage;
        }
    }

    protected void printImage(Printable target, int fromPointY) {
        this.setUpPrint(target);
        int viewportPrintFrom = fromPointY;
        final int viewportHeight = target.getViewportHeight();
        int remainViewPortHeight = viewportHeight;
        while (this.nextPrintableHeight(target, remainViewPortHeight) > 0) {
            int viewportPrinted = this.printInnerScrollElement(target, fromPointY);
            if (viewportPrinted > 0) {
                remainViewPortHeight = this.nextPrintableHeight(target, fromPointY + viewportHeight - viewportPrinted);
                viewportPrintFrom = viewportPrinted;
            }
            if (remainViewPortHeight > 0) {
                BufferedImage part = this.getScreenshot(target, viewportPrintFrom, remainViewPortHeight, viewportHeight);
                int notScrolled = this.appendImageAndScrollVertical(target, part);
                if (notScrolled == 0) {
                    viewportPrintFrom = fromPointY;
                    remainViewPortHeight = viewportHeight;
                } else {
                    int scrollOut = viewportHeight - notScrolled;
                    if (scrollOut < viewportPrinted) {
                        viewportPrinted = viewportPrinted - scrollOut;
                        remainViewPortHeight = this.nextPrintableHeight(target, fromPointY + viewportHeight - viewportPrinted);
                    }
                    remainViewPortHeight = this.nextPrintableHeight(target, remainViewPortHeight);
                    viewportPrintFrom = fromPointY + viewportHeight - remainViewPortHeight;
                }
            }
        }
    }

    protected int printInnerScrollElement(Printable printTarget, int printFrom) {
        int viewportPrint = 0;
        for (Integer pointY : printTarget.getInnerScrollableElement().keySet().stream()
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList())) {
            InnerElement scrollElement = printTarget.getInnerScrollableElement().get(pointY);
            if (this.getPrintedHeight() < pointY && pointY < this.getPrintedHeight() + printTarget.getViewportHeight()) {
                BufferedImage part = this.getScreenshot(printTarget, printFrom, pointY - this.getPrintedHeight());
                int notScrolled = this.appendImageAndScrollVertical(printTarget, part);
                if (notScrolled == 0) {
                    this.appendImage(scrollElement.printImage(new VerticalPrinter(), printFrom));
                } else {
                    viewportPrint = notScrolled;
                    this.appendImage(scrollElement.printImage(new VerticalPrinter(), notScrolled));
                }

                if (printTarget.isMoveScrollTopTo(this.getPrintedHeight() + scrollElement.getViewportHeight())) {
                    this.verticallyScrollOutPrintedPart(printTarget, scrollElement.getViewportHeight());
                    viewportPrint = 0;
                } else {
                    this.appendPrintedHeight(scrollElement.getViewportHeight());
                    if (viewportPrint + scrollElement.getViewportHeight() > printTarget.getViewportHeight()) {
                        printTarget.scrollVertically(printTarget.getScrollableHeight() - printTarget.getViewportHeight());
                        viewportPrint = printTarget.getViewportHeight() - (printTarget.getScrollableHeight() - pointY - scrollElement.getViewportHeight());
                    } else {
                        viewportPrint = viewportPrint + scrollElement.getViewportHeight();
                    }
                }
            }
        }
        return viewportPrint;
    }

    protected BufferedImage getScreenshot(Printable printTarget, int printFrom, int height) {
        return this.getScreenshot(printTarget, printFrom, height, height);
    }

    protected BufferedImage getScreenshot(Printable printTarget, int printFrom, int remainHeight, int viewportHeight) {
        BufferedImage part = printTarget.printImage(new HorizontalPrinter());
        int height = Math.min(part.getHeight(), viewportHeight);
        int width = part.getWidth();
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

    protected void appendImage(BufferedImage part) {
        this.graphics.drawImage(part, 0, this.printedImageHeight, null);
        this.printedImageHeight = this.printedImageHeight + part.getHeight();
    }

    protected int appendImageAndScrollVertical(Printable printTarget, BufferedImage part) {
        this.appendImage(part);
        return this.verticallyScrollOutPrintedPart(printTarget, part.getHeight());
    }

    protected void setUpPrint(Printable printTarget) {
        printTarget.scrollVertically(0);
        this.resetPrintedHeight();
    }

    protected void setPrintedHeight(int printedHeight) {
        this.printedHeight = printedHeight;
    }

    protected void resetPrintedHeight() {
        this.setPrintedHeight(0);
    }

    protected void appendPrintedHeight(int printedHeight) {
        this.setPrintedHeight(this.getPrintedHeight() + printedHeight);
    }

    protected int getPrintedHeight() {
        return this.printedHeight;
    }

    protected int nextPrintableHeight(Printable printTarget, int remainViewPortHeight) {
        return printTarget.nextPrintableHeight(remainViewPortHeight, this.getPrintedHeight());
    }

    protected int verticallyScrollOutPrintedPart(Printable printTarget, int printedHeight) {
        this.appendPrintedHeight(printedHeight);
        int notScrolled = printTarget.scrollOutVertically(printedHeight, this.scrolledHeight);
        if (notScrolled == 0) {
            this.scrolledHeight = this.scrolledHeight + printedHeight;
        } else {
            this.scrolledHeight = this.scrolledHeight + printedHeight - notScrolled;
        }
        return notScrolled;
    }

}
