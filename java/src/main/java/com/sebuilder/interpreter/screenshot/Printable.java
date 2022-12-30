package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.image.BufferedImage;
import java.util.Map;


public interface Printable extends VerticalSurvey, HorizontalSurvey {

    @Override
    default RemoteWebDriver driver() {
        return this.getCtx().driver();
    }

    TestRun getCtx();

    default BufferedImage printImage(final VerticalPrinter aPrinter, final int fromPointY) {
        return aPrinter.getImage(this, fromPointY);
    }

    default BufferedImage printImage(final HorizontalPrinter aPrinter) {
        return aPrinter.getImage(this);
    }

    Map<Integer, InnerElement> getInnerScrollableElement();

}
