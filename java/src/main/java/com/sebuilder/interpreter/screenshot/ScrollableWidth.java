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

    public ScrollableWidth(final Builder builder) {
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
        return this.targetElement;
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

    public boolean isEnableMoveScrollLeftTo(final int aPointX) {
        return aPointX + this.getViewportWidth() < this.getScrollableWidth();
    }

    public void scrollHorizontally(final int nextLeft) {
        if (this.hasHorizontalScroll()) {
            if (this.getTargetElement() != null) {
                this.executeScript("arguments[0].scrollLeft = arguments[1]; return [];", this.getTargetElement(), nextLeft);
            } else {
                this.executeScript("scrollTo(arguments[0],document.documentElement.scrollTop); return [];", nextLeft);
            }
            this.waitForScrolling();
        }
    }

    public int scrollOutHorizontally(final int nextLeft) {
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
            return this.webDriver;
        }

        public WebElement getTargetElement() {
            return this.targetElement;
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

        public boolean isIgnoreScroll() {
            return this.ignoreScroll;
        }

        public Builder setWebDriver(final RemoteWebDriver webDriver) {
            this.webDriver = webDriver;
            return this;
        }

        public Builder setPointX(final int pointX) {
            this.pointX = pointX;
            return this;
        }

        public Builder setViewportWidth(final int viewportWidth) {
            this.viewportWidth = viewportWidth;
            return this;
        }

        public Builder setScrollableWidth(final int scrollableWidth) {
            this.scrollableWidth = scrollableWidth;
            return this;
        }

        public Builder setTargetElement(final WebElement targetElement) {
            this.targetElement = targetElement;
            return this;
        }

        public Builder isIgnoreScroll(final boolean ignoreScroll) {
            this.ignoreScroll = ignoreScroll;
            return this;
        }
    }
}
