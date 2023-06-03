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

import static javafx.concurrent.Worker.State.RUNNING;

public class ReplayPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private HBox runOperation;

    @FXML
    private Button pause;

    @FXML
    private Button screenshot;
    @FXML
    private Button stepOver;

    @FXML
    private Button resume;

    @FXML
    private Button stop;

    @FXML
    private HBox resultOperation;

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

    void populate(final SeInterpreterRunTask task) {
        this.debugger = task.getDebugger();
        this.scriptName.textProperty().bind(task.messageProperty());
        this.scriptDataSetProgress.progressProperty().bind(task.progressProperty());
        this.runStatus.textProperty().bind(task.stateProperty().asString());
        final BooleanBinding taskRunning = task.stateProperty().isEqualTo(RUNNING);
        this.runOperation.visibleProperty().bind(taskRunning);
        this.pause.disableProperty().bind(this.debugger.debugStatusProperty().isEqualTo(Debugger.STATUS.await)
                .or(this.debugger.debugStatusProperty().isEqualTo(Debugger.STATUS.stepOver)));
        this.screenshot.disableProperty().bind(this.debugger.debugStatusProperty().isNotEqualTo(Debugger.STATUS.await));
        this.stepOver.disableProperty().bind(this.debugger.debugStatusProperty().isNotEqualTo(Debugger.STATUS.await));
        this.resume.disableProperty().bind(this.debugger.debugStatusProperty().isNotEqualTo(Debugger.STATUS.await));
        this.stop.disableProperty().bind(this.debugger.debugStatusProperty().isNotEqualTo(Debugger.STATUS.await));
        this.resultOperation.visibleProperty().bind(taskRunning.not());
        this.lastRunResultDir.bind(task.valueProperty());
    }

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
    void handlePause() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.pause());
    }

    @FXML
    void handleTakeScreenshot() {
        new ScreenshotView().open(this.scriptDataSetProgress.getScene().getWindow());
    }

    @FXML
    void handleStepOver() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.stepOver());
    }

    @FXML
    void handleResume() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.resume());
    }

    @FXML
    void handleReplayStop() {
        this.application.stopReplay();
        this.application.executeAndLoggingCaseWhenThrowException(() -> this.debugger.stop());
    }

    @FXML
    void handleOpenReplayLog() {
        this.application.executeAndLoggingCaseWhenThrowException(() ->
                Desktop.getDesktop().open(new File(this.lastRunResultDir.get(), this.application.getReportFileName())));
    }

    @FXML
    void handleOpenDirectory() {
        this.application.executeAndLoggingCaseWhenThrowException(() ->
                Desktop.getDesktop().open(new File(this.lastRunResultDir.get())));
    }

}
