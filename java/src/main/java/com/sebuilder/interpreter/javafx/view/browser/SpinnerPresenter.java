package com.sebuilder.interpreter.javafx.view.browser;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;

public class SpinnerPresenter {

    @FXML
    private ProgressIndicator progressIndicator;

    public void populate(Task<?> task) {
        progressIndicator.progressProperty().bind(task.progressProperty());
    }
}
