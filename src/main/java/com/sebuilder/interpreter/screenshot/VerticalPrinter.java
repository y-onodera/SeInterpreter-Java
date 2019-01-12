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
            int imageHeight = target.getFullImageHeight();
            int imageWidth = target.getFullImageWidth();
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

    protected void setUpPrint(Printable printTarget) {
        printTarget.scrollVertically(0);
        this.resetPrintedHeight();
    }

    protected void resetPrintedHeight() {
        this.setPrintedHeight(0);
    }

    protected int nextPrintableHeight(Printable printTarget, int remainViewPortHeight) {
        return printTarget.nextPrintableHeight(remainViewPortHeight, printTarget.convertDocumentHeight(this.getPrintedHeight()));
    }

    protected int printInnerScrollElement(Printable printTarget, int from) {
        int viewportPrint = 0;
        int printFrom = from;
        for (Integer pointY : printTarget.getInnerScrollableElement().keySet().stream()
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList())) {
            InnerElement scrollElement = printTarget.getInnerScrollableElement().get(pointY);
            if (printTarget.convertDocumentHeight(this.getPrintedHeight()) < pointY
                    && pointY < printTarget.convertDocumentHeight(this.getPrintedHeight()) + printTarget.getViewportHeight()) {
                BufferedImage part = this.getScreenshot(printTarget, printFrom, pointY - printTarget.convertDocumentHeight(this.getPrintedHeight()));
                int notScrolled = this.appendImageAndScrollVertical(printTarget, part);
                if (notScrolled == 0) {
                    this.appendImage(scrollElement.printImage(new VerticalPrinter(), printFrom));
                } else {
                    viewportPrint = notScrolled;
                    this.appendImage(scrollElement.printImage(new VerticalPrinter(), printFrom + notScrolled));
                }

                if (printTarget.isMoveScrollTopTo(printTarget.convertDocumentHeight(this.getPrintedHeight()) + scrollElement.getViewportHeight())) {
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
                printFrom = viewportPrint;
            }
        }
        return viewportPrint;
    }

    protected BufferedImage getScreenshot(Printable printTarget, int printFrom, int height) {
        return this.getScreenshot(printTarget, printFrom, height, height);
    }

    protected BufferedImage getScreenshot(Printable printTarget, int printFrom, int remainHeight, int viewportHeight) {
        BufferedImage part = printTarget.printImage(new HorizontalPrinter());
        return getScreenshot(printTarget.convertImageHeight(printFrom)
                , printTarget.convertImageHeight(remainHeight)
                , printTarget.convertImageHeight(viewportHeight)
                , part);
    }

    protected BufferedImage getScreenshot(int printFrom, int remainHeight, int viewportHeight, BufferedImage part) {
        int height = Math.min(part.getHeight(), viewportHeight);
        int width = part.getWidth();
        if (remainHeight < height) {
            if (printFrom + viewportHeight < part.getHeight()) {
                part = this.getSubImage(printFrom, remainHeight, part, width);
            } else {
                if (printFrom + remainHeight < part.getHeight()) {
                    part = this.getSubImage(printFrom, remainHeight, part, width);
                } else if (printFrom < part.getHeight()) {
                    part = this.getRestImage(printFrom, part, width);
                }
            }
        } else {
            if (printFrom + height < part.getHeight()) {
                part = this.getSubImage(printFrom, height, part, width);
            } else if (printFrom < part.getHeight()) {
                part = this.getRestImage(printFrom, part, width);
            }
        }
        return part;
    }

    protected BufferedImage getSubImage(int printFrom, int remainHeight, BufferedImage part, int width) {
        return part.getSubimage(0, printFrom, width, remainHeight);
    }

    protected BufferedImage getRestImage(int printFrom, BufferedImage part, int width) {
        return part.getSubimage(0, printFrom, width, part.getHeight() - printFrom);
    }

    protected int appendImageAndScrollVertical(Printable printTarget, BufferedImage part) {
        this.appendImage(part);
        return this.verticallyScrollOutPrintedPart(printTarget, part.getHeight());
    }

    protected void appendImage(BufferedImage part) {
        this.graphics.drawImage(part, 0, this.printedImageHeight, null);
        this.printedImageHeight = this.printedImageHeight + part.getHeight();
    }

    protected int verticallyScrollOutPrintedPart(Printable printTarget, int printedHeight) {
        this.appendPrintedHeight(printedHeight);
        return verticallyScrollOut(printTarget, printTarget.convertDocumentHeight(printedHeight));
    }

    protected int verticallyScrollOut(Printable printTarget, int printedHeight) {
        int notScrolled = printTarget.scrollOutVertically(printedHeight, this.scrolledHeight);
        if (notScrolled == 0) {
            this.scrolledHeight = this.scrolledHeight + printedHeight;
        } else {
            this.scrolledHeight = this.scrolledHeight + printedHeight - notScrolled;
        }
        return notScrolled;
    }

    protected int getPrintedHeight() {
        return this.printedHeight;
    }

    protected void setPrintedHeight(int printedHeight) {
        this.printedHeight = printedHeight;
    }

    protected void appendPrintedHeight(int printedHeight) {
        this.setPrintedHeight(this.getPrintedHeight() + printedHeight);
    }
}
