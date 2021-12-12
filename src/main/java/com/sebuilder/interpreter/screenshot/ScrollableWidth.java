package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ScrollableWidth implements Scrollable {
    private final RemoteWebDriver wd;
    private final WebElement targetElement;
    private final int pointX;
    private final int viewportWidth;
    private final int scrollableWidth;
    private final boolean ignoreScroll;

    public ScrollableWidth(Builder builder) {
        this.wd = builder.getWebDriver();
        this.targetElement = builder.getTargetElement();
        this.pointX = builder.getPointX();
        this.viewportWidth = builder.getViewportWidth();
        this.scrollableWidth = builder.getScrollableWidth();
        this.ignoreScroll = builder.isIgnoreScroll();
    }

    @Override
    public RemoteWebDriver driver() {
        return this.wd;
    }

    public WebElement getTargetElement() {
        return targetElement;
    }

    public int getPointX() {
        return this.pointX;
    }

    public int getViewportWidth() {
        return this.viewportWidth;
    }

    public int getScrollableWidth() {
        return this.scrollableWidth;
    }

    public int getScrollWidth() {
        return this.getScrollableWidth() - this.getViewportWidth();
    }

    public boolean hasHorizontalScroll() {
        return !this.ignoreScroll && this.getScrollableWidth() > this.getViewportWidth();
    }

    public boolean isEnableMoveScrollLeftTo(int aPointX) {
        return aPointX + this.getViewportWidth() < this.getScrollableWidth();
    }

    public void scrollHorizontally(int nextLeft) {
        if (this.hasHorizontalScroll()) {
            if (this.getTargetElement() != null) {
                this.executeScript("arguments[0].scrollLeft = arguments[1]; return [];", this.getTargetElement(), nextLeft);
            } else {
                this.executeScript("scrollTo(arguments[0],document.documentElement.scrollTop); return [];", nextLeft);
            }
            waitForScrolling();
        }
    }

    public int scrollOutHorizontally(int nextLeft) {
        if (this.isEnableMoveScrollLeftTo(nextLeft)) {
            this.scrollHorizontally(nextLeft);
            return 0;
        }
        if (this.getViewportWidth() >= this.getScrollableWidth()) {
            return nextLeft;
        }
        final int scrollX = this.getScrollableWidth() - this.getViewportWidth();
        this.scrollHorizontally(scrollX);
        return nextLeft - scrollX;
    }

    public static class Builder {
        private RemoteWebDriver webDriver;
        private WebElement targetElement;
        private int pointX;
        private int viewportWidth;
        private int scrollableWidth;
        private boolean ignoreScroll;

        public ScrollableWidth build() {
            return new ScrollableWidth(this);
        }

        public RemoteWebDriver getWebDriver() {
            return webDriver;
        }

        public WebElement getTargetElement() {
            return this.targetElement;
        }

        public int getPointX() {
            return pointX;
        }

        public int getViewportWidth() {
            return viewportWidth;
        }

        public int getScrollableWidth() {
            return scrollableWidth;
        }

        public boolean isIgnoreScroll() {
            return ignoreScroll;
        }

        public Builder setWebDriver(RemoteWebDriver webDriver) {
            this.webDriver = webDriver;
            return this;
        }

        public Builder setPointX(int pointX) {
            this.pointX = pointX;
            return this;
        }

        public Builder setViewportWidth(int viewportWidth) {
            this.viewportWidth = viewportWidth;
            return this;
        }

        public Builder setScrollableWidth(int scrollableWidth) {
            this.scrollableWidth = scrollableWidth;
            return this;
        }

        public Builder setTargetElement(WebElement targetElement) {
            this.targetElement = targetElement;
            return this;
        }

        public Builder isIgnoreScroll(boolean ignoreScroll) {
            this.ignoreScroll = ignoreScroll;
            return this;
        }
    }
}
