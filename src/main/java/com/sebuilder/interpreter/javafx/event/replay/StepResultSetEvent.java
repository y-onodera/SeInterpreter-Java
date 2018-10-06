package com.sebuilder.interpreter.javafx.event.replay;

import com.sebuilder.interpreter.javafx.Result;

public class StepResultSetEvent {

    private final int stepNo;

    private final Result result;

    public StepResultSetEvent(int stepNo, Result result) {
        this.stepNo = stepNo;
        this.result = result;
    }

    public int getStepNo() {
        return stepNo;
    }

    public Result getResult() {
        return result;
    }

}
