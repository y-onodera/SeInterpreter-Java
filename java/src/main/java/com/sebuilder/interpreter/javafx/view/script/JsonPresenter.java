package com.sebuilder.interpreter.javafx.view.script;

import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.application.ViewType;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import javax.inject.Inject;

public class JsonPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private TextArea textAreaStep;

    @FXML
    void initialize() {
        this.application.displayTestCaseProperty().addListener((observed, oldValue, newValue) -> {
            if (this.application.scriptViewTypeProperty().get() == ViewType.TEXT) {
                this.showScriptAsText();
            }
        });
        this.application.scriptViewTypeProperty().addListener((final ObservableValue<? extends ViewType> observed, final ViewType oldValue, final ViewType newValue) -> {
            if (newValue == ViewType.TEXT) {
                this.showScriptAsText();
            }
        });
    }

    @FXML
    void jsonCommit() {
        this.application.replaceScript(this.textAreaStep.getText());
    }

    private void showScriptAsText() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.textAreaStep.clear();
            this.textAreaStep.setText(this.application.getCurrentDisplayAsJson());
        });
    }
}
