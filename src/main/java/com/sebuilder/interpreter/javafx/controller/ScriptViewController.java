package com.sebuilder.interpreter.javafx.controller;

import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveAsEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultResetEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptAddEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptDeleteEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptInsertEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptSelectEvent;
import com.sebuilder.interpreter.javafx.event.view.OpenScriptSaveChooserEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshScriptViewEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Objects;

public class ScriptViewController {

    @FXML
    private TreeView<String> treeViewScriptName;

    @FXML
    void initialize() {
        assert this.treeViewScriptName != null : "fx:id=\"treeViewScriptName\" was not injected: check your FXML file 'scriptview.fxml'.";
        EventBus.registSubscriber(this);
    }

    @FXML
    void handleScriptInsert(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new ScriptInsertEvent());
    }

    @FXML
    void handleScriptAdd(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new ScriptAddEvent());
    }

    @FXML
    void handleScriptCreateTemplate(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(this.getClass().getResource("/fxml/exportsetting.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage exportSettingDialog = new Stage();
        exportSettingDialog.setScene(scene);
        exportSettingDialog.initOwner(treeViewScriptName.getScene().getWindow());
        exportSettingDialog.initModality(Modality.WINDOW_MODAL);
        exportSettingDialog.setResizable(false);
        exportSettingDialog.setTitle("edit export setting");
        exportSettingDialog.show();
    }

    @FXML
    void handleScriptDelete(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new ScriptDeleteEvent());
    }

    @FXML
    void handleScriptSave(ActionEvent event) {
        EventBus.publish(new FileSaveEvent());
    }

    @FXML
    void handleScriptSaveAs(ActionEvent event) {
        FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Save Script File");
        fileSave.getExtensionFilters().add(new FileChooser.ExtensionFilter("json format (*.json)", "*.json"));
        fileSave.setInitialDirectory(Context.getInstance().getBaseDirectory());
        File file = fileSave.showSaveDialog(treeViewScriptName.getScene().getWindow());
        if (file != null) {
            EventBus.publish(new FileSaveAsEvent(file));
        }
    }

    @Subscribe
    public void showScriptView(RefreshScriptViewEvent aEvent) {
        EventBus.publish(new StepResultResetEvent());
        TreeItem<String> root = new TreeItem<>(aEvent.getFileName());
        root.setExpanded(true);
        this.treeViewScriptName.setRoot(root);
        this.refreshScriptView(aEvent.getScripts(), aEvent.getSelectScriptName());
    }

    @Subscribe
    public void scriptSaveAs(OpenScriptSaveChooserEvent event) {
        this.handleScriptSaveAs(null);
    }

    private void refreshScriptView(LinkedHashMap<String, Script> scripts, String selectScriptName) {
        this.treeViewScriptName.getRoot().getChildren().clear();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            for (String name : scripts.keySet()) {
                TreeItem<String> item = new TreeItem<>(name);
                this.treeViewScriptName.getRoot().getChildren().add(item);
                if (name.equals(selectScriptName)) {
                    this.treeViewScriptName.getSelectionModel().select(item);
                }
            }
            this.treeViewScriptName.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            EventBus.publish(new ScriptSelectEvent(newValue.getValue()));
                        }
                    });
            EventBus.publish(new ScriptSelectEvent(selectScriptName));
        });
    }
}
