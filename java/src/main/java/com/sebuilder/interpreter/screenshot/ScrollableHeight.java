package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ScrollableHeight implements Scrollable {

    private final RemoteWebDriver wd;
    private final WebElement targetElement;
    private final int pointY;
    private final int viewportHeight;
    private final int scrollableHeight;
    private final boolean ignoreScroll;

    public ScrollableHeight(Builder builder) {
        this.wd = builder.getWebDriver();
        this.targetElement = builder.getTargetElement();
        this.pointY = builder.getPointY();
        this.viewportHeight = builder.getViewportHeight();
        this.scrollableHeight = builder.getScrollableHeight();
        this.ignoreScroll = builder.isIgnoreScroll();
    }

    @Override
    public RemoteWebDriver driver() {
        return this.wd;
    }

    public WebElement getTargetElement() {
        return targetElement;
    }

    public int getViewportHeight() {
        return this.viewportHeight;
    }

    public int getScrollableHeight() {
        return this.scrollableHeight;
    }

    public int getPointY() {
        return this.pointY;
    }

    public int getScrollHeight() {
        return this.getScrollableHeight() - this.getViewportHeight();
    }

    public boolean hasVerticalScroll() {
        return !this.ignoreScroll && this.getScrollableHeight() > this.getViewportHeight();
    }

    public boolean isEnableMoveScrollTopTo(int aPointY) {
        return aPointY + this.getViewportHeight() < this.getScrollableHeight();
    }

    public void scrollVertically(int nextTop) {
        if (this.hasVerticalScroll()) {
            if (this.getTargetElement() != null) {
                this.executeScript("arguments[0].scrollTop = arguments[1]; return [];", this.getTargetElement(), nextTop);
            } else {
                this.executeScript("scrollTo(document.documentElement.scrollLeft,arguments[0]); return [];", nextTop);
            }
            waitForScrolling();
        }
    }

    public int scrollOutVertically(int toBeScroll, int scrolledHeight) {
        int nextScrollTop = toBeScroll + scrolledHeight;
        if (this.isEnableMoveScrollTopTo(nextScrollTop)) {
            this.scrollVertically(nextScrollTop);
            return 0;
        }
        if (this.getViewportHeight() >= this.getScrollableHeight()) {
            return nextScrollTop;
        }
        final int scrollY = this.getScrollableHeight() - this.getViewportHeight();
        this.scrollVertically(scrollY);
        return nextScrollTop - scrollY;
    }

    public static class Builder {
        private RemoteWebDriver webDriver;
        private WebElement targetElement;
        private int pointY;
        private int viewportHeight;
        private int scrollableHeight;
        private boolean ignoreScroll;

        public ScrollableHeight build() {
            return new ScrollableHeight(this);
        }

        public RemoteWebDriver getWebDriver() {
            return webDriver;
        }

        public int getPointY() {
            return pointY;
        }

        public int getViewportHeight() {
            return viewportHeight;
        }

        public int getScrollableHeight() {
            return scrollableHeight;
        }

        public WebElement getTargetElement() {
            return this.targetElement;
        }

        public boolean isIgnoreScroll() {
            return this.ignoreScroll;
        }

        public Builder setWebDriver(RemoteWebDriver remoteWebDriver) {
            this.webDriver = remoteWebDriver;
            return this;
        }

        public Builder setPointY(int pointY) {
            this.pointY = pointY;
            return this;
        }

        public Builder setViewportHeight(int viewportHeight) {
            this.viewportHeight = viewportHeight;
            return this;
        }

        public Builder setScrollableHeight(int scrollableHeight) {
            this.scrollableHeight = scrollableHeight;
            return this;
        }

        public Builder setTargetElement(WebElement targetElement) {
            this.targetElement = targetElement;
            return this;
        }

        public ScrollableHeight.Builder isIgnoreScroll(boolean ignoreScroll) {
            this.ignoreScroll = ignoreScroll;
            return this;
        }
    }
}
