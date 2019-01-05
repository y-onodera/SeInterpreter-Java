package com.sebuilder.interpreter.screenshot;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HorizontalPrinter {

    private int printedWidth;
    private int printedImageWidth;
    private BufferedImage finalImage;
    private Graphics2D graphics;

    public BufferedImage getImage(HorizontalSurvey target) {
        if (!target.hasHorizontalScroll()) {
            return this.getScreenshot(target, 0, target.getWindowWidth());
        }
        try {
            int imageHeight = target.getWindowHeight();
            int imageWidth = target.getWindowWidth() + target.getScrollWidth() + target.getInnerScrollWidth();
            this.finalImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
            this.graphics = this.finalImage.createGraphics();
            this.printImage(target);
        } finally {
            this.graphics.dispose();
            return finalImage;
        }
    }

    protected void printImage(HorizontalSurvey target) {
        this.setUpPrint(target);
        int fromPointX = target.getPointX();
        int viewportPrintFrom = fromPointX;
        int viewportWidth = target.getViewportWidth();
        int remainViewPortWidth = viewportWidth;
        while (this.nextPrintableWidth(target, remainViewPortWidth) > 0) {
            if (remainViewPortWidth > 0) {
                BufferedImage part = this.getScreenshot(target, viewportPrintFrom, remainViewPortWidth, viewportWidth);
                int notScrolled = this.appendImageAndScrollHorizontal(target, part);
                if (notScrolled == 0) {
                    viewportPrintFrom = fromPointX;
                    remainViewPortWidth = viewportWidth;
                } else {
                    remainViewPortWidth = this.nextPrintableWidth(target, remainViewPortWidth);
                    viewportPrintFrom = fromPointX + viewportWidth - remainViewPortWidth;
                }
            }
        }
        final int printed = target.getPointX() + target.getViewportWidth();
        this.appendImage(this.getScreenshot(target, printed, target.getWindowWidth() - printed));
    }

    protected void setUpPrint(HorizontalSurvey printTarget) {
        printTarget.scrollHorizontally(0);
        this.resetPrintedWidth();
        this.appendImage(this.getScreenshot(printTarget, 0, printTarget.getPointX()));
    }

    protected void resetPrintedWidth() {
        this.setPrintedWidth(0);
    }

    protected void setPrintedWidth(int printedWidth) {
        this.printedWidth = printedWidth;
    }

    protected int nextPrintableWidth(HorizontalSurvey printTarget, int remainViewPortWidth) {
        return Math.min(remainViewPortWidth, printTarget.getScrollableWidth() - this.getPrintedWidth());
    }

    protected int getPrintedWidth() {
        return this.printedWidth;
    }

    protected BufferedImage getScreenshot(HorizontalSurvey printTarget, int printFrom, int width) {
        return this.getScreenshot(printTarget, printFrom, width, width);
    }

    protected BufferedImage getScreenshot(HorizontalSurvey printTarget, int printFrom, int remainWidth, int viewportWidth) {
        BufferedImage part = printTarget.getScreenshot();
        int height = part.getHeight();
        int width = Math.min(part.getWidth(), viewportWidth);
        if (remainWidth < width) {
            if (printFrom + viewportWidth < part.getWidth()) {
                part = part.getSubimage(printFrom, 0, remainWidth, height);
            } else {
                if (printFrom + remainWidth < part.getWidth()) {
                    part = part.getSubimage(printFrom, 0, remainWidth, height);
                } else if (printFrom < part.getWidth()) {
                    part = part.getSubimage(printFrom, 0, part.getWidth() - printFrom, height);
                }
            }
        } else {
            if (printFrom + width < part.getWidth()) {
                part = part.getSubimage(printFrom, 0, width, height);
            } else if (printFrom < part.getWidth()) {
                part = part.getSubimage(printFrom, 0, part.getWidth() - printFrom, height);
            }
        }
        return part;
    }

    protected int appendImageAndScrollHorizontal(HorizontalSurvey printTarget, BufferedImage part) {
        this.appendImage(part);
        return this.horizontallyScrollOutPrintedPart(printTarget, part.getWidth());
    }

    protected void appendImage(BufferedImage part) {
        this.graphics.drawImage(part, this.printedImageWidth, 0, null);
        this.printedImageWidth = this.printedImageWidth + part.getWidth();
    }

    protected int horizontallyScrollOutPrintedPart(HorizontalSurvey printTarget, int printedWidth) {
        this.appendPrintedWidth(printedWidth);
        return printTarget.scrollOutHorizontally(this.getPrintedWidth());
    }

    protected void appendPrintedWidth(int printedWidth) {
        this.setPrintedWidth(this.getPrintedWidth() + printedWidth);
    }

}