package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.WebDriverWrapper;

public interface Scrollable extends WebDriverWrapper {

    default long scrollTimeout() {
        return 100;
    }

    default void waitForScrolling() {
        try {
            Thread.sleep(this.scrollTimeout());
        } catch (final InterruptedException var2) {
            throw new IllegalStateException("Exception while waiting for scrolling", var2);
        }
    }
}
