package com.sebuilder.interpreter.javafx.controller;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.script.RefreshStepViewEvent;
import com.sebuilder.interpreter.javafx.event.script.RefreshScriptViewEvent;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.script.ResetStepResutEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;

public class ScriptViewController {

    @FXML
    private TreeView<String> treeViewScriptName;

    @FXML
    void initialize() {
        assert treeViewScriptName != null : "fx:id=\"treeViewScriptName\" was not injected: check your FXML file 'seleniumbuilderscriptview.fxml'.";
        EventBus.registSubscriber(this);
    }

    @Subscribe
    public void showScriptView(RefreshScriptViewEvent event) {
        EventBus.publish(new ResetStepResutEvent());
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            Map<String, Script> scripts = Maps.newHashMap();
            TreeItem<String> root = new TreeItem<>(event.getFileName());
            root.setExpanded(true);
            for (Script script : event.getScripts()) {
                TreeItem<String> item = new TreeItem<>(script.name);
                scripts.put(script.name, script);
                root.getChildren().add(item);
            }
            this.treeViewScriptName.setRoot(root);
            this.treeViewScriptName.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            EventBus.publish(new RefreshStepViewEvent(scripts.get(newValue.getValue())));
                        }
                    });
            EventBus.publish(new RefreshStepViewEvent(new Script()));
        });
    }
}
