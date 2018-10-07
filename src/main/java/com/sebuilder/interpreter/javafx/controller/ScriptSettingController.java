package com.sebuilder.interpreter.javafx.controller;

import com.sebuilder.interpreter.javafx.EventBus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

public class ScriptSettingController {

    @FXML
    private TextField datasourceText;

    @FXML
    private Button datasourceSearchButton;

    @FXML
    private Button editButton;

    private final File currentDir = Paths.get(".").toAbsolutePath().normalize().toFile();

    @FXML
    void initialize() {
        assert datasourceText != null : "fx:id=\"datasourceText\" was not injected: check your FXML file 'seleniumbuilderbrowsersetting.fxml'.";
        assert datasourceSearchButton != null : "fx:id=\"datasourceSearchButton\" was not injected: check your FXML file 'seleniumbuilderbrowsersetting.fxml'.";
        assert editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'seleniumbuilderbrowsersetting.fxml'.";

        EventBus.registSubscriber(this);
    }

    @FXML
    void datasourceSearch(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Resource File");
        directoryChooser.setInitialDirectory(this.currentDir);
        Stage stage = new Stage();
        stage.initOwner(this.datasourceText.getScene().getWindow());
        File file = directoryChooser.showDialog(stage);
        this.datasourceText.setText(file.getAbsolutePath());
    }

    @FXML
    void settingEdit(ActionEvent event) {
        this.datasourceText.getScene().getWindow().hide();
    }

}
