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
    private Tab stepText;

    @FXML
    private Tab stepTable;

    @FXML
    private TextArea textAreaScriptLog;

    @FXML
    void initialize() {
        TextAreaAppender.setTextArea(this.textAreaScriptLog);
        this.stepText.setOnSelectionChanged(event -> {
            if (this.stepText.isSelected()) {
                this.application.changeScriptViewType(ViewType.TEXT);
            }
        });
        this.stepTable.setOnSelectionChanged(event -> {
            if (this.stepTable.isSelected()) {
                this.application.changeScriptViewType(ViewType.TABLE);
            }
        });
        this.application.changeScriptViewType(ViewType.TABLE);
    }

}
