package com.sebuilder.interpreter.javafx.view.menu;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.view.replay.InputView;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.util.Optional;

public class MenuPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private Pane paneSeInterpreterMenu;

    @FXML
    void initialize() {
        assert paneSeInterpreterMenu != null : "fx:id=\"paneSeInterpreterMenu\" was not injected: check your FXML file 'menu.fxml'.";
    }

    @FXML
    void handleScriptOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json format (*.json)", "*.json"));
        fileChooser.setInitialDirectory(this.getBaseDirectory());
        Stage stage = new Stage();
        stage.initOwner(paneSeInterpreterMenu.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            this.application.scriptReLoad(file);
        }
    }

    @FXML
    void handleSaveSuite() {
        if (Strings.isNullOrEmpty(this.application.getSuite().path())) {
            this.saveSuiteToNewFile();
        } else {
            this.application.saveSuite();
        }
    }

    @FXML
    void handleSaveSuiteAs() {
        this.saveSuiteToNewFile();
    }

    @FXML
    void handleCreateNewSuite() {
        this.application.reset();
    }

    @FXML
    void handleBrowserOpen() {
        this.application.browserOpen();
    }

    @FXML
    void handleBrowserClose() {
        this.application.browserClose();
    }

    @FXML
    void handleBrowserSetting()  {
        new BrowserView().open(this.paneSeInterpreterMenu.getScene().getWindow());
    }

    @FXML
    void handleReplaySuite() {
        this.application.runSuite();
    }

    @FXML
    void handleReplayScript() {
        new InputView().open(paneSeInterpreterMenu.getScene().getWindow());
    }

    @FXML
    void handleReplaySetting() {
        new DatasourceView().open(paneSeInterpreterMenu.getScene().getWindow());
    }

    @FXML
    void handleOpenResult() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> Desktop.getDesktop()
                .open(Context.getResultOutputDirectory()));
    }

    private void saveSuiteToNewFile() {
        FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Save Suite File");
        fileSave.getExtensionFilters().add(new FileChooser.ExtensionFilter("json format (*.json)", "*.json"));
        fileSave.setInitialDirectory(this.getBaseDirectory());
        Stage stage = new Stage();
        stage.initOwner(this.paneSeInterpreterMenu.getScene().getWindow());
        File file = fileSave.showSaveDialog(stage);
        if (file != null) {
            this.application.saveSuite(file);
        }
    }

    private File getBaseDirectory() {
        return Optional.ofNullable(this.application.getSuite().head().relativePath())
                .orElse(Context.getBaseDirectory());
    }

}
