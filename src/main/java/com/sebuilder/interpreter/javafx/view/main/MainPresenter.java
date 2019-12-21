package com.sebuilder.interpreter.javafx.view.main;

import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.application.ViewType;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import javax.inject.Inject;

public class MainPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private Tab tabStepText;

    @FXML
    private Tab tabStepTable;

    @FXML
    private TextArea textAreaScriptLog;

    @FXML
    void initialize() {
        assert tabStepTable != null : "fx:id=\"tabStepTable\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        assert tabStepText != null : "fx:id=\"tabStepText\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        assert textAreaScriptLog != null : "fx:id=\"textAreaScriptLog\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        TextAreaAppender.setTextArea(textAreaScriptLog);
        tabStepText.setOnSelectionChanged(event -> {
            if (tabStepText.isSelected()) {
                this.application.changeScriptViewType(ViewType.TEXT);
            }
        });
        tabStepTable.setOnSelectionChanged(event -> {
            if (tabStepTable.isSelected()) {
                this.application.changeScriptViewType(ViewType.TABLE);
            }
        });
        this.application.changeScriptViewType(ViewType.TABLE);
    }

}
