package com.sebuilder.interpreter.javafx.view.main;

import com.sebuilder.interpreter.javafx.model.ViewType;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

import java.util.function.Consumer;

public class MainPresenter {

    @FXML
    private Tab stepText;

    @FXML
    private Tab stepTable;

    @FXML
    private TextArea textAreaScriptLog;

    private Consumer<ViewType> handleViewSelected;

    public void setHandleViewSelected(final Consumer<ViewType> handleViewSelected) {
        this.handleViewSelected = handleViewSelected;
        TextAreaAppender.setTextArea(this.textAreaScriptLog);
        this.stepText.setOnSelectionChanged(event -> {
            if (this.stepText.isSelected()) {
                this.handleViewSelected.accept(ViewType.TEXT);
            }
        });
        this.stepTable.setOnSelectionChanged(event -> {
            if (this.stepTable.isSelected()) {
                this.handleViewSelected.accept(ViewType.TABLE);
            }
        });
        this.handleViewSelected.accept(ViewType.TABLE);
    }
}
