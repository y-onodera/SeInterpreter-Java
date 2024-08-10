package com.sebuilder.interpreter.javafx.view.step;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StepDefine {

    private final ObjectProperty<StepNo> no;

    private final StringProperty script;

    private final StringProperty runningResult;

    public StepDefine(final StepNo stepNo, final String step, final String runningResult) {
        this.no = new SimpleObjectProperty<>(stepNo);
        this.script = new SimpleStringProperty(step);
        if (runningResult != null) {
            this.runningResult = new SimpleStringProperty(runningResult);
        } else {
            this.runningResult = new SimpleStringProperty();
        }
    }

    public ObjectProperty<StepNo> noProperty() {
        return this.no;
    }

    public StringProperty scriptProperty() {
        return this.script;
    }

    public StringProperty runningResultProperty() {
        return this.runningResult;
    }

    public void setRunningResult(final String runningResult) {
        this.runningResult.set(runningResult);
    }

    public int compareIndex(final int index) {
        return Integer.compare(this.index(), index);
    }

    public int index() {
        return this.no.get().no() - 1;
    }

}
