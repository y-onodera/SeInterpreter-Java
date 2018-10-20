package com.sebuilder.interpreter.javafx.controller;

import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveAsEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultResetEvent;
import com.sebuilder.interpreter.javafx.event.replay.TemplateLoadEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptAddEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptDeleteEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptInsertEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptSelectEvent;
import com.sebuilder.interpreter.javafx.event.view.OpenScriptSaveChooserEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshScriptViewEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.LinkedHashMap;

public class ScriptViewController {

    @FXML
    private TreeView<String> treeViewScriptName;

    @FXML
    void initialize() {
        assert this.treeViewScriptName != null : "fx:id=\"treeViewScriptName\" was not injected: check your FXML file 'seleniumbuilderscriptview.fxml'.";
        EventBus.registSubscriber(this);
    }

    @Subscribe
    public void showScriptView(RefreshScriptViewEvent aEvent) {
        EventBus.publish(new StepResultResetEvent());
        TreeItem<String> root = new TreeItem<>(aEvent.getFileName());
        root.setExpanded(true);
        this.treeViewScriptName.setRoot(root);
        this.refleshScriptView(aEvent.getScripts());
    }

    @Subscribe
    public void scriptSaveAs(OpenScriptSaveChooserEvent event) {
        this.handleScriptSaveAs(null);
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
    void handleScriptCreateTemplate(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        EventBus.publish(new TemplateLoadEvent());
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
        fileSave.setTitle("Close Resource File");
        fileSave.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("select target script", ".json"));
        fileSave.setInitialDirectory(Context.getInstance().getBaseDirectory());
        File file = fileSave.showSaveDialog(treeViewScriptName.getScene().getWindow());
        if (file != null) {
            EventBus.publish(new FileSaveAsEvent(file));
        }
    }


    private void refleshScriptView(LinkedHashMap<String, Script> scripts) {
        this.treeViewScriptName.getRoot().getChildren().clear();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            for (String name : scripts.keySet()) {
                TreeItem<String> item = new TreeItem<>(name);
                this.treeViewScriptName.getRoot().getChildren().add(item);
            }
            this.treeViewScriptName.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            EventBus.publish(new ScriptSelectEvent(newValue.getValue()));
                        }
                    });
            MultipleSelectionModel msm = this.treeViewScriptName.getSelectionModel();
            TreeItem<String> lastItem = this.treeViewScriptName.getRoot().getChildren().get(scripts.size() - 1);
            int row = this.treeViewScriptName.getRow(lastItem);
            msm.select(row);
            EventBus.publish(new ScriptSelectEvent(lastItem.getValue()));
        });
    }

}
