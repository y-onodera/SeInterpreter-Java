package com.sebuilder.interpreter.screenshot;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Comparator;

public class VerticalPrinter {

    private int printedHeight;
    private int printedImageHeight;
    private int scrolledHeight;
    private Graphics2D graphics;

    public BufferedImage getImage(final Printable target, final int fromPointY) {
        BufferedImage finalImage;
        try {
            final int imageHeight = target.getFullImageHeight();
            final int imageWidth = target.getFullImageWidth();
            finalImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
            this.graphics = finalImage.createGraphics();
            this.printImage(target, fromPointY);
        } finally {
            this.graphics.dispose();
        }
        return finalImage;
    }

    protected void printImage(final Printable target, final int fromPointY) {
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
                final BufferedImage part = this.getScreenshot(target, viewportPrintFrom, remainViewPortHeight, viewportHeight);
                final int notScrolled = this.appendImageAndScrollVertical(target, part, target.convertImageHeight(viewportPrintFrom - fromPointY));
                if (notScrolled == 0) {
                    viewportPrintFrom = fromPointY;
                    remainViewPortHeight = viewportHeight;
                } else {
                    final int scrollOut = viewportHeight - notScrolled;
                    if (scrollOut == 0) {
                        remainViewPortHeight = 0;
                    } else if (scrollOut < viewportPrinted) {
                        viewportPrinted = viewportPrinted - scrollOut;
                        remainViewPortHeight = this.nextPrintableHeight(target, fromPointY + viewportHeight - viewportPrinted);
                    }
                    remainViewPortHeight = this.nextPrintableHeight(target, remainViewPortHeight);
                    viewportPrintFrom = fromPointY + viewportHeight - remainViewPortHeight;
                }
            }
        }
    }

    protected void setUpPrint(final Printable printTarget) {
        printTarget.scrollVertically(0);
        this.resetPrintedHeight();
    }

    protected void resetPrintedHeight() {
        this.setPrintedHeight(0);
    }

    protected int nextPrintableHeight(final Printable printTarget, final int remainViewPortHeight) {
        return printTarget.nextPrintableHeight(remainViewPortHeight, printTarget.convertDocumentHeight(this.getPrintedHeight()));
    }

    protected int printInnerScrollElement(final Printable printTarget, final int from) {
        int viewportPrint = 0;
        int printFrom = from;
        for (final Integer pointY : printTarget.getInnerScrollableElement().keySet().stream()
                .sorted(Comparator.comparing(Integer::intValue))
                .toList()) {
            final InnerElement scrollElement = printTarget.getInnerScrollableElement().get(pointY);
            if (printTarget.convertDocumentHeight(this.getPrintedHeight()) < pointY
                    && pointY < printTarget.convertDocumentHeight(this.getPrintedHeight()) + printTarget.getViewportHeight()) {
                final BufferedImage part = this.getScreenshot(printTarget, printFrom, pointY - printTarget.convertDocumentHeight(this.getPrintedHeight()));
                final int notScrolled = this.appendImageAndScrollVertical(printTarget, part);
                if (notScrolled == 0) {
                    this.appendImage(scrollElement.printImage(new VerticalPrinter(), printFrom));
                } else {
                    viewportPrint = notScrolled;
                    this.appendImage(scrollElement.printImage(new VerticalPrinter(), printFrom + notScrolled));
                }

                if (printTarget.isEnableMoveScrollTopTo(printTarget.convertDocumentHeight(this.getPrintedHeight()) + scrollElement.getViewportHeight())) {
                    this.verticallyScrollOutPrintedPart(printTarget, printTarget.convertImageHeight(scrollElement.getViewportHeight()));
                    viewportPrint = 0;
                } else {
                    this.appendPrintedHeight(printTarget.convertImageHeight(scrollElement.getViewportHeight()));
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

    protected BufferedImage getScreenshot(final Printable printTarget, final int printFrom, final int height) {
        return this.getScreenshot(printTarget, printFrom, height, height);
    }

    protected BufferedImage getScreenshot(final Printable printTarget, final int printFrom, final int remainHeight, final int viewportHeight) {
        final BufferedImage part = printTarget.printImage(new HorizontalPrinter());
        return this.getScreenshot(printTarget.convertImageHeight(printFrom)
                , printTarget.convertImageHeight(remainHeight)
                , printTarget.convertImageHeight(viewportHeight)
                , part);
    }

    protected BufferedImage getScreenshot(final int printFrom, final int remainHeight, final int viewportHeight, BufferedImage part) {
        final int height = Math.min(part.getHeight(), viewportHeight);
        final int width = part.getWidth();
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

    protected BufferedImage getSubImage(final int printFrom, final int remainHeight, final BufferedImage part, final int width) {
        return part.getSubimage(0, printFrom, width, remainHeight);
    }

    protected BufferedImage getRestImage(final int printFrom, final BufferedImage part, final int width) {
        return part.getSubimage(0, printFrom, width, part.getHeight() - printFrom);
    }

    protected int appendImageAndScrollVertical(final Printable printTarget, final BufferedImage part, final int prePrintedHeight) {
        this.appendImage(part);
        return this.verticallyScrollOutPrintedPart(printTarget, part.getHeight(), prePrintedHeight);
    }

    protected int appendImageAndScrollVertical(final Printable printTarget, final BufferedImage part) {
        return this.appendImageAndScrollVertical(printTarget, part, 0);
    }

    protected void appendImage(final BufferedImage part) {
        this.graphics.drawImage(part, 0, this.printedImageHeight, null);
        this.printedImageHeight = this.printedImageHeight + part.getHeight();
    }

    protected int verticallyScrollOutPrintedPart(final Printable printTarget, final int printedHeight, final int prePrintedHeight) {
        this.appendPrintedHeight(printedHeight);
        return this.verticallyScrollOut(printTarget, printTarget.convertDocumentHeight(printedHeight + prePrintedHeight));
    }

    protected void verticallyScrollOutPrintedPart(final Printable printTarget, final int printedHeight) {
        this.verticallyScrollOutPrintedPart(printTarget, printedHeight, 0);
    }

    protected int verticallyScrollOut(final Printable printTarget, final int toBeScroll) {
        final int notScrolled = printTarget.scrollOutVertically(toBeScroll, this.scrolledHeight);
        if (notScrolled == 0) {
            this.scrolledHeight = this.scrolledHeight + toBeScroll;
        } else {
            this.scrolledHeight = this.scrolledHeight + toBeScroll - notScrolled;
        }
        return notScrolled;
    }

    protected int getPrintedHeight() {
        return this.printedHeight;
    }

    protected void setPrintedHeight(final int printedHeight) {
        this.printedHeight = printedHeight;
    }

    protected void appendPrintedHeight(final int printedHeight) {
        this.setPrintedHeight(this.getPrintedHeight() + printedHeight);
    }
}
