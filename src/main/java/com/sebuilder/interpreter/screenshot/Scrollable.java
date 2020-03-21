package com.sebuilder.interpreter.screenshot;

public interface Scrollable {

    default long scrollTimeout() {
        return 100;
    }

    default void waitForScrolling() {
        try {
            Thread.sleep(this.scrollTimeout());
        } catch (InterruptedException var2) {
            throw new IllegalStateException("Exception while waiting for scrolling", var2);
        }
    }
}
