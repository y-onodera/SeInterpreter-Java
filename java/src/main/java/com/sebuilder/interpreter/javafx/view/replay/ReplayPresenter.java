package com.sebuilder.interpreter.javafx.view.replay;

import com.sebuilder.interpreter.javafx.application.Debugger;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.application.SeInterpreterRunTask;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static javafx.concurrent.Worker.State.RUNNING;

public class ReplayPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    public HBox runOperation;

    @FXML
    public Button stepOver;

    @FXML
    public Button resume;

    @FXML
    public Button pause;

    @FXML
    private Button stop;

    @FXML
    public HBox resultOperation;

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
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.stepOver());
    }

    @FXML
    public void handlePause() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.pause());
    }

    @FXML
    public void handleResume() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.resume());
    }

    @FXML
    void handleReplayStop() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.stop());
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

    public void populate(final SeInterpreterRunTask task) {
        this.debugger = task.getDebugger();
        this.scriptName.textProperty().bind(task.messageProperty());
        this.scriptDataSetProgress.progressProperty().bind(task.progressProperty());
        this.runStatus.textProperty().bind(task.stateProperty().asString());
        final BooleanBinding taskRunning = task.stateProperty().isEqualTo(RUNNING);
        this.runOperation.visibleProperty().bind(taskRunning);
        this.stepOver.disableProperty().bind(this.debugger.debugStatusProperty().isNotEqualTo(Debugger.STATUS.await));
        this.resume.disableProperty().bind(this.debugger.debugStatusProperty().isNotEqualTo(Debugger.STATUS.await));
        this.pause.disableProperty().bind(this.debugger.debugStatusProperty().isEqualTo(Debugger.STATUS.await)
                .or(this.debugger.debugStatusProperty().isEqualTo(Debugger.STATUS.stepOver)));
        this.stop.disableProperty().bind(this.debugger.debugStatusProperty().isNotEqualTo(Debugger.STATUS.await));
        this.resultOperation.visibleProperty().bind(taskRunning.not());
        this.lastRunResultDir.bind(task.valueProperty());
    }

}
