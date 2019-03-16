package com.sebuilder.interpreter.javafx.controller;

import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Suite;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveAsEvent;
import com.sebuilder.interpreter.javafx.event.file.FileSaveEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultResetEvent;
import com.sebuilder.interpreter.javafx.event.script.*;
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
    void handleScriptImport(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json format (*.json)", "*.json"));
        fileChooser.setInitialDirectory(Context.getInstance().getBaseDirectory());
        Stage stage = new Stage();
        stage.initOwner(treeViewScriptName.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            EventBus.publish(new ScriptImportEvent(file));
        }
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
        fileSave.setTitle("Save TestCase File");
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
        Suite suite = aEvent.getSuite();
        TreeItem<String> root = new TreeItem<>(suite.getName());
        root.setExpanded(true);
        this.treeViewScriptName.setRoot(root);
        this.refreshScriptView(suite, aEvent.getSelectScriptName());
    }

    @Subscribe
    public void scriptSaveAs(OpenScriptSaveChooserEvent event) {
        this.handleScriptSaveAs(null);
    }

    private void refreshScriptView(Suite suite, String selectScriptName) {
        this.treeViewScriptName.getRoot().getChildren().clear();
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            for (TestCase testCase : suite) {
                String name = testCase.name();
                TreeItem<String> item = new TreeItem<>(name);
                if (!suite.getScenario().isChainTarget(testCase)) {
                    this.treeViewScriptName.getRoot().getChildren().add(item);
                    if (name.equals(selectScriptName)) {
                        this.treeViewScriptName.getSelectionModel().select(item);
                    }
                }
                if (suite.getScenario().hasChain(testCase)) {
                    TestCase before = testCase;
                    while (suite.getScenario().hasChain(before)) {
                        TreeItem<String> chainItem = new TreeItem(suite.getScenario().getChainTo(before).name());
                        item.getChildren().add(chainItem);
                        if (chainItem.getValue().equals(selectScriptName)) {
                            this.treeViewScriptName.getSelectionModel().select(chainItem);
                        }
                        before = suite.getScenario().getChainTo(before);
                    }
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
