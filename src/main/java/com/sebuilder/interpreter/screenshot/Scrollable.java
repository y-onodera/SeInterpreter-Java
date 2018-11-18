package com.sebuilder.interpreter.screenshot;

public interface Scrollable {

    long scrollTimeout();

    default void waitForScrolling() {
        try {
            Thread.sleep(this.scrollTimeout());
        } catch (InterruptedException var2) {
            throw new IllegalStateException("Exception while waiting for scrolling", var2);
        }
    }
}
