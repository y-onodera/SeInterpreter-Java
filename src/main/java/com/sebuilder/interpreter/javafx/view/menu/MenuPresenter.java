package com.sebuilder.interpreter.javafx.view.menu;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MenuPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private Pane paneSeInterpreterMenu;

    private Stage browserSettingDialog;

    private Stage scriptSettingDialog;

    @FXML
    void initialize() {
        assert paneSeInterpreterMenu != null : "fx:id=\"paneSeInterpreterMenu\" was not injected: check your FXML file 'menu.fxml'.";
    }

    @FXML
    void handleScriptOpenFile(ActionEvent event) {
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
    void handleSaveSuite(ActionEvent event) {
        if (Strings.isNullOrEmpty(this.application.getSuite().path())) {
            this.saveSuiteToNewFile();
        } else {
            this.application.saveSuite();
        }
    }

    @FXML
    void handleSaveSuiteAs(ActionEvent event) {
        this.saveSuiteToNewFile();
    }

    @FXML
    void handleCreateNewSuite(ActionEvent event) {
        this.application.reset();
    }

    @FXML
    void handleBrowserOpen(ActionEvent event) {
        this.application.browserOpen();
    }

    @FXML
    void handleBrowserClose(ActionEvent event) {
        this.application.browserClose();
    }

    @FXML
    void handleBrowserSetting(ActionEvent event) throws IOException {
        if (browserSettingDialog == null) {
            BrowserView browserView = new BrowserView();
            Scene scene = new Scene(browserView.getView());
            browserSettingDialog = new Stage();
            browserSettingDialog.setScene(scene);
            browserSettingDialog.initOwner(paneSeInterpreterMenu.getScene().getWindow());
            browserSettingDialog.initModality(Modality.WINDOW_MODAL);
            browserSettingDialog.setResizable(false);
            browserSettingDialog.setTitle("edit browser setting");
        }
        browserSettingDialog.show();
    }

    @FXML
    void handleReplaySetting(ActionEvent event) throws IOException {
        if (scriptSettingDialog == null) {
            DatasourceView datasourceView = new DatasourceView();
            Scene scene = new Scene(datasourceView.getView());
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
    void handleReplaySuite(ActionEvent event) {
        this.application.runSuite();
    }

    @FXML
    void handleReplayScript(ActionEvent event) {
        this.application.runScript();
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
