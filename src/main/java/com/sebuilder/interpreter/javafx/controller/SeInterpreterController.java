package com.sebuilder.interpreter.javafx.controller;

import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.TextAreaAppender;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.ViewType;
import com.sebuilder.interpreter.javafx.event.script.ScriptReplaceEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptViewChangeEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepTextViewEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

public class SeInterpreterController {

    @FXML
    private Tab tabStepText;

    @FXML
    private Tab tabStepTable;

    @FXML
    private TextArea textAreaStep;

    @FXML
    private Button buttonJsonCommit;

    @FXML
    private TextArea textAreaScriptLog;

    @FXML
    void initialize() {
        assert tabStepTable != null : "fx:id=\"tabStepTable\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        assert tabStepText != null : "fx:id=\"tabStepText\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        assert textAreaStep != null : "fx:id=\"textAreaStep\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        assert buttonJsonCommit != null : "fx:id=\"buttonJsonCommit\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        assert textAreaScriptLog != null : "fx:id=\"textAreaScriptLog\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        EventBus.registSubscriber(this);
        TextAreaAppender.setTextArea(textAreaScriptLog);
        tabStepText.setOnSelectionChanged(event -> {
            if (tabStepText.isSelected()) {
                EventBus.publish(new ScriptViewChangeEvent(ViewType.TEXT));
            }
        });
        tabStepTable.setOnSelectionChanged(event -> {
            if (tabStepTable.isSelected()) {
                EventBus.publish(new ScriptViewChangeEvent(ViewType.TABLE));
            }
        });
    }

    @FXML
    void jsonCommit(ActionEvent event) {
        EventBus.publish(new ScriptReplaceEvent(this.textAreaStep.getText()));
    }

    @Subscribe
    public void showScriptAsText(RefreshStepTextViewEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            this.textAreaStep.clear();
            this.textAreaStep.setText(Context.getScriptParser().toString(event.getTestCase()));
        });
    }

    @Subscribe
    public void report(ReportErrorEvent event) {
        if (event == ReportErrorEvent.CLEAR) {
            this.textAreaScriptLog.clear();
            return;
        }
        this.textAreaScriptLog.appendText(Throwables.getStackTraceAsString(event.getSource()));
    }
}
