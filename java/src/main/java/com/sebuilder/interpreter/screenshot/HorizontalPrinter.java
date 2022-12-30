package com.sebuilder.interpreter.screenshot;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HorizontalPrinter {

    private int printedWidth;
    private int printedImageWidth;
    private Graphics2D graphics;

    public BufferedImage getImage(final HorizontalSurvey target) {
        if (!target.hasHorizontalScroll()) {
            return this.getScreenshot(target, 0, target.getWindowWidth());
        }
        BufferedImage finalImage;
        try {
            final int imageHeight = target.getImageHeight();
            final int imageWidth = target.getFullImageWidth();
            finalImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
            this.graphics = finalImage.createGraphics();
            this.printImage(target);
        } finally {
            this.graphics.dispose();
        }
        return finalImage;
    }

    protected void printImage(final HorizontalSurvey target) {
        final int fromPointX = this.setUpPrint(target);
        int viewportPrintFrom = fromPointX;
        final int viewportWidth = target.getViewportWidth();
        int remainViewPortWidth = viewportWidth;
        while (this.nextPrintableWidth(target, remainViewPortWidth) > 0) {
            if (remainViewPortWidth > 0) {
                final BufferedImage part = this.getScreenshot(target, viewportPrintFrom, remainViewPortWidth, viewportWidth);
                final int notScrolled = this.appendImageAndScrollHorizontal(target, part);
                if (notScrolled == 0) {
                    viewportPrintFrom = fromPointX;
                    remainViewPortWidth = viewportWidth;
                } else {
                    remainViewPortWidth = this.nextPrintableWidth(target, remainViewPortWidth);
                    viewportPrintFrom = fromPointX + viewportWidth - remainViewPortWidth;
                }
            }
        }
        final int printed = fromPointX + target.getViewportWidth();
        this.appendImage(this.getScreenshot(target, printed, target.getWindowWidth() - printed));
    }

    protected int setUpPrint(final HorizontalSurvey printTarget) {
        printTarget.scrollHorizontally(0);
        this.resetPrintedWidth();
        if (printTarget.getPointX() > 0) {
            final boolean isParentScrollable = printTarget instanceof InnerElement && printTarget.hasHorizontalScroll();
            if (isParentScrollable) {
                ((InnerElement) printTarget).getParent().scrollHorizontally(0);
            }
            this.appendImage(this.getScreenshot(printTarget, 0, printTarget.getPointX()));
            if (isParentScrollable) {
                return ((InnerElement) printTarget).getParent().scrollOutHorizontally(printTarget.getPointX());
            }
        }
        return printTarget.getPointX();
    }

    protected void resetPrintedWidth() {
        this.setPrintedWidth(0);
    }

    protected void setPrintedWidth(final int printedWidth) {
        this.printedWidth = printedWidth;
    }

    protected int nextPrintableWidth(final HorizontalSurvey printTarget, final int remainViewPortWidth) {
        return Math.min(remainViewPortWidth, printTarget.getScrollableWidth() - printTarget.convertDocumentWidth(this.getPrintedWidth()));
    }

    protected int getPrintedWidth() {
        return this.printedWidth;
    }

    protected BufferedImage getScreenshot(final HorizontalSurvey printTarget, final int printFrom, final int width) {
        return this.getScreenshot(printTarget, printFrom, width, width);
    }

    protected BufferedImage getScreenshot(final HorizontalSurvey printTarget, final int printFrom, final int remainWidth, final int viewportWidth) {
        final BufferedImage part = printTarget.getScreenshot();
        return this.getBufferedImage(printTarget.convertImageWidth(printFrom)
                , printTarget.convertImageWidth(remainWidth)
                , printTarget.convertImageWidth(viewportWidth)
                , part);
    }

    private BufferedImage getBufferedImage(final int printFrom, final int remainWidth, final int viewportWidth, BufferedImage part) {
        final int height = part.getHeight();
        final int width = Math.min(part.getWidth(), viewportWidth);
        if (remainWidth < width) {
            if (printFrom + remainWidth < part.getWidth()) {
                part = this.getBufferedImage(printFrom, remainWidth, part, height);
            } else {
                if (printFrom + remainWidth < part.getWidth()) {
                    part = this.getBufferedImage(part.getWidth() - remainWidth, remainWidth, part, height);
                } else if (printFrom < part.getWidth()) {
                    part = this.getBufferedImage(printFrom, part, height);
                }
            }
        } else {
            if (printFrom + width < part.getWidth()) {
                part = this.getBufferedImage(printFrom, width, part, height);
            } else if (printFrom < part.getWidth()) {
                part = this.getBufferedImage(printFrom, part, height);
            }
        }
        return part;
    }

    private BufferedImage getBufferedImage(final int printFrom, final BufferedImage part, final int height) {
        return part.getSubimage(printFrom, 0, part.getWidth() - printFrom, height);
    }

    private BufferedImage getBufferedImage(final int printFrom, final int remainWidth, final BufferedImage part, final int height) {
        return part.getSubimage(printFrom, 0, remainWidth, height);
    }

    protected int appendImageAndScrollHorizontal(final HorizontalSurvey printTarget, final BufferedImage part) {
        this.appendImage(part);
        return this.horizontallyScrollOutPrintedPart(printTarget, part.getWidth());
    }

    protected void appendImage(final BufferedImage part) {
        this.graphics.drawImage(part, this.printedImageWidth, 0, null);
        this.printedImageWidth = this.printedImageWidth + part.getWidth();
    }

    protected int horizontallyScrollOutPrintedPart(final HorizontalSurvey printTarget, final int printedWidth) {
        this.appendPrintedWidth(printedWidth);
        return printTarget.scrollOutHorizontally(printTarget.convertDocumentWidth(this.getPrintedWidth()));
    }

    protected void appendPrintedWidth(final int printedWidth) {
        this.setPrintedWidth(this.getPrintedWidth() + printedWidth);
    }

}
