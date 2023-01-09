package com.sebuilder.interpreter.javafx.view.replay;

import com.sebuilder.interpreter.javafx.application.Debugger;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static javafx.concurrent.Worker.State.RUNNING;

public class ReplayPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    public Button stepOver;

    @FXML
    private Button stop;

    @FXML
    private Button openLog;

    @FXML
    private Button openDir;

    @FXML
    private ProgressBar scriptDataSetProgress;

    @FXML
    private Label scriptName;

    @FXML
    private Label runStatus;

    private final StringProperty lastRunResultDir = new SimpleStringProperty();

    private Debugger debugger;

    @FXML
    void initialize() {
        assert this.stop != null : "fx:id=\"stop\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert this.scriptDataSetProgress != null : "fx:id=\"scriptDataSetProgress\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert this.openDir != null : "fx:id=\"openDir\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert this.scriptName != null : "fx:id=\"scriptName\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert this.runStatus != null : "fx:id=\"runStatus\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert this.openLog != null : "fx:id=\"openLog\" was not injected: check your FXML file 'runprogress.fxml'.";
    }

    @FXML
    public void handleStepOver() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.debugger.stepOver();
        });
    }

    @FXML
    void handleReplayStop() {
        if (this.stepOver.isVisible()) {
            this.application.executeAndLoggingCaseWhenThrowException(() -> {
                this.debugger.stop();
            });
        }
        this.application.stopReplay();
    }

    @FXML
    void handleOpenReplayLog() throws IOException {
        Desktop.getDesktop().open(new File(this.lastRunResultDir.get(), this.application.getReportFileName()));
    }

    @FXML
    void handleOpenDirectory() throws IOException {
        Desktop.getDesktop().open(new File(this.lastRunResultDir.get()));
    }

    public void bind(final Task<String> task, final Debugger debugger) {
        this.scriptName.textProperty().bind(task.messageProperty());
        this.scriptDataSetProgress.progressProperty().bind(task.progressProperty());
        this.runStatus.textProperty().bind(task.stateProperty().asString());
        this.stop.disableProperty().bind(task.stateProperty().isNotEqualTo(RUNNING));
        this.openLog.disableProperty().bind(task.stateProperty().isEqualTo(RUNNING));
        this.openDir.disableProperty().bind(task.stateProperty().isEqualTo(RUNNING));
        this.lastRunResultDir.bind(task.valueProperty());
        this.debugger = debugger;
        this.stepOver.visibleProperty().set(debugger != null);
        if (this.debugger != null) {
            this.stepOver.disableProperty().bind(this.debugger.disableProperty());
        }
    }

}
