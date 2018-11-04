package com.sebuilder.interpreter.javafx.controller;

import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.replay.StopEvent;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import static javafx.concurrent.Worker.State.RUNNING;

public class RunningProgressController {

    @FXML
    private Button stop;

    @FXML
    private ProgressBar scriptDataSetProgress;

    @FXML
    private Label scriptName;

    @FXML
    private Label runStatus;

    @FXML
    void initialize() {
        assert stop != null : "fx:id=\"stop\" was not injected: check your FXML file 'seleniumbuilderRunProgress.fxml'.";
        assert scriptDataSetProgress != null : "fx:id=\"scriptDataSetProgress\" was not injected: check your FXML file 'seleniumbuilderRunProgress.fxml'.";
        assert scriptName != null : "fx:id=\"scriptName\" was not injected: check your FXML file 'seleniumbuilderRunProgress.fxml'.";
        assert runStatus != null : "fx:id=\"runStatus\" was not injected: check your FXML file 'seleniumbuilderRunProgress.fxml'.";
    }

    @FXML
    void handleReplayStop(ActionEvent event) {
        EventBus.publish(new StopEvent());
    }

    public void bind(Task task) {
        this.scriptName.textProperty().bind(task.messageProperty());
        this.scriptDataSetProgress.progressProperty().bind(task.progressProperty());
        this.runStatus.textProperty().bind(task.stateProperty().asString());
        this.stop.disableProperty().bind(task.stateProperty().isNotEqualTo(RUNNING));
    }
}
