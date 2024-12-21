package com.sebuilder.interpreter.javafx.view.suite;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.HasFileChooser;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import com.sebuilder.interpreter.javafx.view.filter.FilterTablePresenter;
import com.sebuilder.interpreter.pointcut.ImportFilter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;

public class StepFilterPresenter implements HasFileChooser {

    @Inject
    private SeInterpreter application;
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
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() ->
                this.filterTableController.addListener((observed, oldValue, newValue) -> {
                    if (newValue != null) {
                        this.currentProperty.set(newValue);
                    }
                }));
    }

    @FXML
    void commit() {
        this.onCommit.accept(this.currentProperty.get());
        this.dialog.close();
    }

    @FXML
    public void saveAs() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final File target = this.saveDialog("Save Filter File", "json format (*.json)", "*.json");
            Files.writeString(target.toPath(), Context.getTestCaseConverter().toString(this.currentProperty.get()), StandardCharsets.UTF_8);
            this.currentProperty.set(new ImportFilter(this.application.getCurrentRootDir().toPath()
                    .relativize(target.toPath()).toString().replace("\\", "/")
                    , "", (path) -> Context.getScriptParser().loadPointCut(path, this.application.getCurrentRootDir())));
            this.filterTableController.setTarget(this.currentProperty.get());
            SuccessDialog.show("save succeed");
        });
    }

    @Override
    public Window currentWindow() {
        return this.dialog.getOwner();
    }
}
