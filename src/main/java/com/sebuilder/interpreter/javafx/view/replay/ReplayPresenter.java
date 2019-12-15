package com.sebuilder.interpreter.javafx.view.replay;

import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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

    private StringProperty lastRunResultDir = new SimpleStringProperty();

    @FXML
    void initialize() {
        assert stop != null : "fx:id=\"stop\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert scriptDataSetProgress != null : "fx:id=\"scriptDataSetProgress\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert openDir != null : "fx:id=\"openDir\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert scriptName != null : "fx:id=\"scriptName\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert runStatus != null : "fx:id=\"runStatus\" was not injected: check your FXML file 'runprogress.fxml'.";
        assert openLog != null : "fx:id=\"openLog\" was not injected: check your FXML file 'runprogress.fxml'.";
    }

    @FXML
    void handleReplayStop(ActionEvent event) throws Exception {
        this.application.stopReplay();
    }

    @FXML
    void handleOpenReplayLog(ActionEvent event) throws IOException {
        Desktop.getDesktop().open(new File(this.lastRunResultDir.get(), "junit-noframes.html"));
    }

    @FXML
    void handleOpenDirectory(ActionEvent event) throws IOException {
        Desktop.getDesktop().open(new File(this.lastRunResultDir.get()));
    }

    public void bind(Task task) {
        this.scriptName.textProperty().bind(task.messageProperty());
        this.scriptDataSetProgress.progressProperty().bind(task.progressProperty());
        this.runStatus.textProperty().bind(task.stateProperty().asString());
        this.stop.disableProperty().bind(task.stateProperty().isNotEqualTo(RUNNING));
        this.openLog.disableProperty().bind(task.stateProperty().isEqualTo(RUNNING));
        this.openDir.disableProperty().bind(task.stateProperty().isEqualTo(RUNNING));
        this.lastRunResultDir.bind(task.valueProperty());
    }
}
