package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;



public class Page extends AbstractPrintable {
    private int windowHeight;
    private int windowWidth;
    private int scrollableHeight;
    private int viewportHeight;
    private int scrollableWidth;
    private int viewportWidth;

    public Page(TestRun ctx, long scrollTimeout, InnerScrollElementHandler innerScrollElementHandler) {
        super(ctx, scrollTimeout, innerScrollElementHandler);
        this.windowHeight = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return window.innerHeight || document.documentElement.clientHeight || document.getElementsByTagName('body')[0].clientHeight;", new Object[0])).intValue();
        this.windowWidth = ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return window.innerWidth || document.documentElement.clientWidth || document.getElementsByTagName('body')[0].clientWidth;", new Object[0])).intValue();
        this.viewportHeight = this.getWindowHeight();
        this.viewportWidth = this.getWindowWidth();
        this.scrollableHeight = this.getFullHeight();
        this.scrollableWidth = this.getFullWidth();
    }

    @Override
    public int getWindowHeight() {
        return this.windowHeight;
    }

    @Override
    public int getWindowWidth() {
        return this.windowWidth;
    }

    @Override
    public int getViewportHeight() {
        return viewportHeight;
    }

    @Override
    public int getScrollableHeight() {
        return scrollableHeight;
    }

    @Override
    public int getViewportWidth() {
        return viewportHeight;
    }

    @Override
    public int getScrollableWidth() {
        return scrollableHeight;
    }

}
