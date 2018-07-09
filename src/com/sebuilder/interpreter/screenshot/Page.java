package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Page extends AbstractPrintable {
    private int pageWidth;
    private int printedImageHeight;
    private BufferedImage finalImage;
    private Graphics2D graphics;

    public Page(TestRun ctx, long scrollTimeout, InnerElementScrollStrategy innerElementScrollStrategy) {
        super(ctx, scrollTimeout, innerElementScrollStrategy);
        int imageHeight = this.getScrollableHeight() + this.getInnerScrollHeight();
        this.finalImage = new BufferedImage(this.getWindowWidth(), imageHeight, BufferedImage.TYPE_3BYTE_BGR);
        this.graphics = this.finalImage.createGraphics();
    }

    public BufferedImage getFinalImage() {
        try {
            this.printImage(0);
        } catch (Throwable e) {
            getCtx().log().error(e);
        } finally {
            this.graphics.dispose();
            return finalImage;
        }
    }

    @Override
    public void setUpPrint(int fromPointY) {
        this.scrollVertically(fromPointY);
        this.resetPrintedHeight();
    }

    @Override
    public int getWindowWidth() {
        if (pageWidth == 0) {
            pageWidth = super.getWindowWidth();
        }
        return pageWidth;
    }

    @Override
    public void appendImage(BufferedImage part) {
        this.graphics.drawImage(part, 0, this.printedImageHeight, null);
        this.printedImageHeight = this.printedImageHeight + part.getHeight();
    }

}
