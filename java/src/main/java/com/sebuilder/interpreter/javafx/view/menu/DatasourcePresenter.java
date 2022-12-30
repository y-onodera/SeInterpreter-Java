package com.sebuilder.interpreter.javafx.view.menu;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.File;

public class DatasourcePresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private TextField datasourceText;

    @FXML
    private Button datasourceSearchButton;

    @FXML
    private Button editButton;

    @FXML
    void initialize() {
        assert this.datasourceText != null : "fx:id=\"datasourceText\" was not injected: check your FXML file 'browsersetting.fxml'.";
        assert this.datasourceSearchButton != null : "fx:id=\"datasourceSearchButton\" was not injected: check your FXML file 'browsersetting.fxml'.";
        assert this.editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'browsersetting.fxml'.";
        if (Context.getDataSourceDirectory().exists()) {
            this.datasourceText.setText(Context.getDataSourceDirectory().getAbsolutePath());
        } else {
            this.datasourceText.setText(Context.getBaseDirectory().getAbsolutePath());
        }
    }

    @FXML
    void dataSourceSearch(final ActionEvent event) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Resource File");
        directoryChooser.setInitialDirectory(new File(this.datasourceText.getText()));
        final Stage stage = new Stage();
        stage.initOwner(this.datasourceText.getScene().getWindow());
        final File file = directoryChooser.showDialog(stage);
        if (file != null && file.exists()) {
            this.datasourceText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void settingEdit(final ActionEvent event) {
        Context.getInstance().setDataSourceDirectory(this.datasourceText.getText());
        this.datasourceText.getScene().getWindow().hide();
    }
}
