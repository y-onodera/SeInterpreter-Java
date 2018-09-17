package com.sebuilder.interpreter.javafx.controller;


import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.browser.BrowserCloseEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserOpenEvent;
import com.sebuilder.interpreter.javafx.event.browser.LoadTemplateEvent;
import com.sebuilder.interpreter.javafx.event.file.FileLoadEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveEvent;
import com.sebuilder.interpreter.javafx.event.replay.ResetStepResultEvent;
import com.sebuilder.interpreter.javafx.event.replay.RunEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptResetEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class MenuController {

    @FXML
    private Pane paneSeInterpreterMenu;

    @FXML
    void handleScriptNew(ActionEvent event) {
        EventBus.publish(new ResetStepResultEvent());
        EventBus.publish(new ScriptResetEvent());
    }

    @FXML
    void handleScriptOpenFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("select target script", ".json"));
        Stage stage = new Stage();
        stage.initOwner(paneSeInterpreterMenu.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            EventBus.publish(new FileLoadEvent(file));
        }
    }

    @FXML
    void handleScriptSave(ActionEvent event) {
        FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Close Resource File");
        fileSave.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("select target script", ".json"));
        File file = fileSave.showSaveDialog(paneSeInterpreterMenu.getScene().getWindow());
        if (file != null) {
            EventBus.publish(new FileSaveEvent(file));
        }
    }

    @FXML
    void handleBrowserOpen(ActionEvent event) {
        EventBus.publish(new ResetStepResultEvent());
        EventBus.publish(new BrowserOpenEvent());
    }

    @FXML
    void handleBrowserRunScript(ActionEvent event) {
        EventBus.publish(new ResetStepResultEvent());
        EventBus.publish(new RunEvent());
    }

    @FXML
    void handleBrowserClose(ActionEvent event) {
        EventBus.publish(new ResetStepResultEvent());
        EventBus.publish(new BrowserCloseEvent());
    }

    @FXML
    void handleBrowserCreateTemplate(ActionEvent event) {
        EventBus.publish(new ResetStepResultEvent());
        EventBus.publish(new LoadTemplateEvent());
    }

    @FXML
    void handleBrowserSetting(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/seleniumbuilderbrowsersetting.fxml")));
        Scene scene = new Scene(root);
        Stage dialog = new Stage();
        dialog.setScene(scene);
        dialog.initOwner(paneSeInterpreterMenu.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(false);
        dialog.setTitle("edit browser setting");
        dialog.show();
    }

    @FXML
    void initialize() {
        assert paneSeInterpreterMenu != null : "fx:id=\"paneSeInterpreterMenu\" was not injected: check your FXML file 'seleniumbuildermenu.fxml'.";
    }

}

