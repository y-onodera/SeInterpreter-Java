package com.sebuilder.interpreter.javafx.controller;

import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.replay.StopEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static javafx.concurrent.Worker.State.RUNNING;

public class RunningProgressController {

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

    public static void init(Window window, Task task) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(RunningProgressController.class.getResource("/fxml/runprogress.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage runProgressDialog = new Stage();
        runProgressDialog.setScene(scene);
        runProgressDialog.initOwner(window);
        runProgressDialog.initModality(Modality.WINDOW_MODAL);
        runProgressDialog.setTitle("run progress");
        RunningProgressController controller = loader.getController();
        controller.bind(task);
        runProgressDialog.setResizable(false);
        runProgressDialog.show();
    }

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
    void handleReplayStop(ActionEvent event) {
        EventBus.publish(new StopEvent());
    }

    @FXML
    void handleOpenReplayLog(ActionEvent event) throws IOException {
        Desktop.getDesktop().open(new File(this.lastRunResultDir.get(), "junit-noframes.html"));
    }

    @FXML
    void handleOpenDirectory(ActionEvent event) throws IOException {
        Desktop.getDesktop().open(new File(this.lastRunResultDir.get()));
    }

    private void bind(Task task) {
        this.scriptName.textProperty().bind(task.messageProperty());
        this.scriptDataSetProgress.progressProperty().bind(task.progressProperty());
        this.runStatus.textProperty().bind(task.stateProperty().asString());
        this.stop.disableProperty().bind(task.stateProperty().isNotEqualTo(RUNNING));
        this.openLog.disableProperty().bind(task.stateProperty().isEqualTo(RUNNING));
        this.openDir.disableProperty().bind(task.stateProperty().isEqualTo(RUNNING));
        this.lastRunResultDir.bind(task.valueProperty());
    }
}
