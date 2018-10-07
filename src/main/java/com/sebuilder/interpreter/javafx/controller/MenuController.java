package com.sebuilder.interpreter.javafx.controller;


import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.browser.BrowserCloseEvent;
import com.sebuilder.interpreter.javafx.event.browser.BrowserOpenEvent;
import com.sebuilder.interpreter.javafx.event.file.FileLoadEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveAsEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveEvent;
import com.sebuilder.interpreter.javafx.event.replay.RunEvent;
import com.sebuilder.interpreter.javafx.event.replay.RunSuiteEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultResetEvent;
import com.sebuilder.interpreter.javafx.event.replay.TemplateLoadEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptAddEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptResetEvent;
import com.sebuilder.interpreter.javafx.event.view.OpenScriptSaveChooserEvent;
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

    private Stage browserSettingDialog;

    private Stage scriptSettingDialog;

    @Subscribe
    public void scriptSaveAs(OpenScriptSaveChooserEvent event) {
        this.handleScriptSaveAs(null);
    }

    @FXML
    void initialize() {
        assert paneSeInterpreterMenu != null : "fx:id=\"paneSeInterpreterMenu\" was not injected: check your FXML file 'seleniumbuildermenu.fxml'.";
        EventBus.registSubscriber(this);
    }

    @FXML
    void handleScriptNew(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new ScriptResetEvent());
    }

    @FXML
    void handleScriptAdd(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new ScriptAddEvent());
    }

    @FXML
    void handleScriptOpenFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("select target script", ".json"));
        fileChooser.setInitialDirectory(Context.getInstance().getBaseDirectory());
        Stage stage = new Stage();
        stage.initOwner(paneSeInterpreterMenu.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            EventBus.publish(new FileLoadEvent(file));
        }
    }

    @FXML
    void handleScriptSave(ActionEvent event) {
        EventBus.publish(new FileSaveEvent());
    }

    @FXML
    void handleScriptSaveAs(ActionEvent event) {
        FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Close Resource File");
        fileSave.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("select target script", ".json"));
        fileSave.setInitialDirectory(Context.getInstance().getBaseDirectory());
        File file = fileSave.showSaveDialog(paneSeInterpreterMenu.getScene().getWindow());
        if (file != null) {
            EventBus.publish(new FileSaveAsEvent(file));
        }
    }

    @FXML
    void handleScriptSetting(ActionEvent event) throws IOException {
        if (scriptSettingDialog == null) {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/seleniumbuilderscriptsetting.fxml")));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scriptSettingDialog = new Stage();
            scriptSettingDialog.setScene(scene);
            scriptSettingDialog.initOwner(paneSeInterpreterMenu.getScene().getWindow());
            scriptSettingDialog.initModality(Modality.WINDOW_MODAL);
            scriptSettingDialog.setResizable(false);
            scriptSettingDialog.setTitle("edit script setting");
        }
        scriptSettingDialog.show();
    }

    @FXML
    void handleBrowserOpen(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new BrowserOpenEvent());
    }

    @FXML
    void handleBrowserRunSuite(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new RunSuiteEvent());
    }

    @FXML
    void handleBrowserRunScript(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new RunEvent());
    }

    @FXML
    void handleBrowserClose(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new BrowserCloseEvent());
    }

    @FXML
    void handleBrowserCreateTemplate(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new TemplateLoadEvent());
    }

    @FXML
    void handleBrowserSetting(ActionEvent event) throws IOException {
        if (browserSettingDialog == null) {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/seleniumbuilderbrowsersetting.fxml")));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            browserSettingDialog = new Stage();
            browserSettingDialog.setScene(scene);
            browserSettingDialog.initOwner(paneSeInterpreterMenu.getScene().getWindow());
            browserSettingDialog.initModality(Modality.WINDOW_MODAL);
            browserSettingDialog.setResizable(false);
            browserSettingDialog.setTitle("edit browser setting");
        }
        browserSettingDialog.show();
    }

}

