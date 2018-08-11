package com.sebuilder.interpreter.javafx.controller;

import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.TextAreaAppender;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SeInterpreterController {

    @FXML
    private TextArea textAreaScriptLog;

    @FXML
    void initialize() {
        assert textAreaScriptLog != null : "fx:id=\"textAreaScriptLog\" was not injected: check your FXML file 'seleniumbuilder.fxml'.";
        EventBus.registSubscriber(this);
        TextAreaAppender.setTextArea(textAreaScriptLog);
    }

    @Subscribe
    public void report(ReportErrorEvent event) {
        if (event == ReportErrorEvent.CLEAR) {
            this.textAreaScriptLog.clear();
            return;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        event.getSource().printStackTrace(pw);
        String sStackTrace = sw.toString();
        this.textAreaScriptLog.setText(sStackTrace);
    }
}
