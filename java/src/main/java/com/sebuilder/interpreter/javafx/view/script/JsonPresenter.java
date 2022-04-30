package com.sebuilder.interpreter.javafx.view.script;

import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.application.ViewType;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javax.inject.Inject;

public class JsonPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private TextArea textAreaStep;

    @FXML
    private Button buttonJsonCommit;

    @FXML
    void initialize() {
        assert textAreaStep != null : "fx:id=\"textAreaStep\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        assert buttonJsonCommit != null : "fx:id=\"buttonJsonCommit\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        this.application.displayTestCaseProperty().addListener((observed, oldValue, newValue) -> {
            if (application.scriptViewTypeProperty().get() == ViewType.TEXT) {
                showScriptAsText();
            }
        });
        this.application.scriptViewTypeProperty().addListener((ObservableValue<? extends ViewType> observed, ViewType oldValue, ViewType newValue) -> {
            if (newValue == ViewType.TEXT) {
                showScriptAsText();
            }
        });
    }

    @FXML
    void jsonCommit() {
        this.application.replaceScript(this.textAreaStep.getText());
    }

    void showScriptAsText() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.textAreaStep.clear();
            this.textAreaStep.setText(this.application.getCurrentDisplayAsJson());
        });
    }
}
