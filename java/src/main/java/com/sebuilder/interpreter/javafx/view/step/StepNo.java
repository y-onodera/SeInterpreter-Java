package com.sebuilder.interpreter.javafx.view.step;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public record StepNo(Integer no, Circle breakPoint) {
    public StepNo(final Integer no) {
        this(no, null);
    }

    public StepNo withBreakPoint() {
        return new StepNo(this.no, new Circle(3, Color.RED));
    }
}
