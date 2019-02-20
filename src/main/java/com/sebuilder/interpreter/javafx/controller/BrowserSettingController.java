package com.sebuilder.interpreter.javafx.controller;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.browser.BrowserSettingEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class BrowserSettingController {

    @FXML
    private ComboBox<String> browserSelect;

    @FXML
    private Button driverSearchButton;

    @FXML
    private TextField driverText;

    @FXML
    private Button editButton;

    private String selectedBrowser;

    private String currentDriverPath;

    private File parentDir;

    @FXML
    void initialize() {
        assert browserSelect != null : "fx:id=\"browserSelect\" was not injected: check your FXML file 'browsersetting.fxml'.";
        assert driverSearchButton != null : "fx:id=\"driverSearchButton\" was not injected: check your FXML file 'browsersetting.fxml'.";
        assert driverText != null : "fx:id=\"driverText\" was not injected: check your FXML file 'browsersetting.fxml'.";
        assert editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'browsersetting.fxml'.";
        this.init("Chrome", System.getProperty("webdriver.chrome.driver"));
    }

    @FXML
    void selectBrowser(ActionEvent event) {
        String browser = browserSelect.getSelectionModel().getSelectedItem();
        if (this.selectedBrowser.equals(browser)) {
            return;
        }
        this.selectedBrowser = this.browserSelect.getSelectionModel().getSelectedItem();
        this.populate();
    }

    @FXML
    public void setDriverPath(ActionEvent actionEvent) {
        this.currentDriverPath = this.driverText.getText();
        if (!Strings.isNullOrEmpty(this.currentDriverPath)) {
            this.parentDir = new File(this.currentDriverPath).getParentFile().getAbsoluteFile();
        }
    }

    @FXML
    void driverSearch(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(this.parentDir);
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("select driver.exe", ".exe"));
        Stage stage = new Stage();
        stage.initOwner(this.driverText.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null && file.exists()) {
            this.parentDir = file.getParentFile().getAbsoluteFile();
            this.driverText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void settingEdit(ActionEvent event) {
        EventBus.publish(new BrowserSettingEvent(this.selectedBrowser, this.driverText.getText()));
        this.driverText.getScene().getWindow().hide();
    }

    private void init(String selectedBrowser, String currentDriverPath) {
        this.selectedBrowser = selectedBrowser;
        this.currentDriverPath = currentDriverPath;
        this.browserSelect.getItems().add("Chrome");
        this.browserSelect.getItems().add("Firefox");
        this.browserSelect.getItems().add("InternetExplorer");
        this.browserSelect.getItems().add("Edge");
        if (this.selectedBrowser != null) {
            this.browserSelect.getSelectionModel().select(this.selectedBrowser);
        } else {
            this.browserSelect.getSelectionModel().select(0);
        }
        if (this.currentDriverPath != null) {
            this.driverText.setText(this.currentDriverPath);
            this.parentDir = new File(this.currentDriverPath).getParentFile().getAbsoluteFile();
        } else {
            this.populate();
        }
    }

    private void populate() {
        String driverName = "chromedriver.exe";
        if ("Firefox".equals(selectedBrowser)) {
            driverName = "geckodriver.exe";
        } else if ("Edge".equals(selectedBrowser)) {
            driverName = "MicrosoftWebDriver.exe";
        } else if ("InternetExplorer".equals(selectedBrowser)) {
            driverName = "IEDriverServer.exe";
        }
        if (parentDir == null || !parentDir.exists()) {
            File driverParent = new File(Context.getInstance().getBaseDirectory(), "exe/");
            if (!driverParent.exists()) {
                driverParent = Context.getInstance().getBaseDirectory();
            }
            this.parentDir = driverParent.getAbsoluteFile();
        }
        this.driverText.setText(new File(parentDir, driverName).getAbsolutePath());
    }

}
