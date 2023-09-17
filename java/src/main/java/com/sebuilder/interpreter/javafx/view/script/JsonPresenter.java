package com.sebuilder.interpreter.javafx.view.script;

import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.model.ViewType;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import javax.inject.Inject;

public class JsonPresenter {

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private TextArea textAreaStep;

    @FXML
    void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.application.displayTestCase().addListener((observed, oldValue, newValue) -> {
                if (this.application.scriptViewType().get() == ViewType.TEXT) {
                    this.showScriptAsText();
                }
            });
            this.application.scriptViewType().addListener((final ObservableValue<? extends ViewType> observed, final ViewType oldValue, final ViewType newValue) -> {
                if (newValue == ViewType.TEXT) {
                    this.showScriptAsText();
                }
            });
        });
    }

    @FXML
    void jsonCommit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.application.replaceScript(this.textAreaStep.getText());
            SuccessDialog.show("commit succeed");
        });
    }

    private void showScriptAsText() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.textAreaStep.clear();
            this.textAreaStep.setText(this.application.getCurrentDisplayAsJson());
        });
    }
}
