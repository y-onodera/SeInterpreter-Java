package com.sebuilder.interpreter.javafx.view.suite;

import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.filter.FilterTablePresenter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.util.function.Consumer;

public class StepFilterPresenter {

    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private FilterTablePresenter filterTableController;

    private final ObjectProperty<Pointcut> currentProperty = new SimpleObjectProperty<>();

    private Consumer<Pointcut> onCommit = (pointcut) -> {
    };
    private Stage dialog;

    public void populate(final Stage dialog, final Pointcut init, final Consumer<Pointcut> onCommit) {
        this.dialog = dialog;
        this.onCommit = onCommit;
        this.currentProperty.set(init);
        this.filterTableController.setTarget(init);
    }

    public void setDefaultValue(final Pointcut defaultValue) {
        this.filterTableController.setDefaultValue(defaultValue);
    }

    @FXML
    void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.filterTableController.addListener((final ObservableValue<? extends Pointcut> observed, final Pointcut oldValue, final Pointcut newValue) -> {
                if (newValue != null) {
                    this.currentProperty.set(newValue);
                }
            });
        });
    }

    @FXML
    void commit() {
        this.onCommit.accept(this.currentProperty.get());
        this.dialog.close();
    }

}
