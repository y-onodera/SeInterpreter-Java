package com.sebuilder.interpreter.javafx.controller;

import com.sebuilder.interpreter.Context;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class ScriptSettingController {

    @FXML
    private TextField datasourceText;

    @FXML
    private Button datasourceSearchButton;

    @FXML
    private Button editButton;

    @FXML
    void initialize() {
        assert datasourceText != null : "fx:id=\"datasourceText\" was not injected: check your FXML file 'browsersetting.fxml'.";
        assert datasourceSearchButton != null : "fx:id=\"datasourceSearchButton\" was not injected: check your FXML file 'browsersetting.fxml'.";
        assert editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'browsersetting.fxml'.";

        if (Context.getDataSourceDirectory().exists()) {
            this.datasourceText.setText(Context.getDataSourceDirectory().getAbsolutePath());
        } else {
            this.datasourceText.setText(Context.getBaseDirectory().getAbsolutePath());
        }
    }

    @FXML
    void dataSourceSearch(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Resource File");
        directoryChooser.setInitialDirectory(new File(this.datasourceText.getText()));
        Stage stage = new Stage();
        stage.initOwner(this.datasourceText.getScene().getWindow());
        File file = directoryChooser.showDialog(stage);
        if (file != null && file.exists()) {
            this.datasourceText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void settingEdit(ActionEvent event) {
        Context.getInstance().setDataSourceDirectory(this.datasourceText.getText());
        this.datasourceText.getScene().getWindow().hide();
    }

}
