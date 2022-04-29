package com.sebuilder.interpreter.javafx.view.menu;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.WebDriverFactory;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.File;
import java.util.Objects;

public class BrowserPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private ComboBox<String> browserSelect;

    @FXML
    private TextField remoteUrl;

    @FXML
    private TextField driverText;

    @FXML
    private TextField binaryText;

    @FXML
    private Button binarySearchButton;

    private String selectedBrowser;

    private String currentDriverPath;

    private File parentDir;

    @FXML
    void initialize() {
        this.init(Context.getRemoteUrl(), Context.getBrowser(), Context.getWebDriverFactory().getDriverPath());
    }

    @FXML
    void selectBrowser() {
        String browser = browserSelect.getSelectionModel().getSelectedItem();
        if (Objects.equals(this.selectedBrowser, browser)) {
            return;
        }
        this.selectedBrowser = this.browserSelect.getSelectionModel().getSelectedItem();
        this.populate();
    }

    @FXML
    void setDriverPath() {
        this.currentDriverPath = this.driverText.getText();
        if (!Strings.isNullOrEmpty(this.currentDriverPath)) {
            this.parentDir = new File(this.currentDriverPath).getParentFile().getAbsoluteFile();
        }
    }

    @FXML
    void driverSearch() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(this.parentDir);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("select driver.exe", "*.exe"));
        Stage stage = new Stage();
        stage.initOwner(this.driverText.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null && file.exists()) {
            this.parentDir = file.getParentFile().getAbsoluteFile();
            this.driverText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void binarySearch() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(this.parentDir);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("select browser.exe", "*.exe"));
        Stage stage = new Stage();
        stage.initOwner(this.binaryText.getScene().getWindow());
        File file = fileChooser.showOpenDialog(stage);
        if (file != null && file.exists()) {
            this.binaryText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void settingEdit() {
        this.application.browserSetting(this.selectedBrowser
                , this.remoteUrl.getText()
                , this.driverText.getText()
                , this.binaryText.getText());
        this.driverText.getScene().getWindow().hide();
    }

    private void init(String remoteUrl, String aSelectedBrowser, String aCurrentDriverPath) {
        this.browserSelect.getItems().add("Chrome");
        this.browserSelect.getItems().add("Firefox");
        this.browserSelect.getItems().add("InternetExplorer");
        this.browserSelect.getItems().add("Edge");
        if (!Strings.isNullOrEmpty(aSelectedBrowser)) {
            this.browserSelect.getSelectionModel().select(aSelectedBrowser);
        } else {
            this.browserSelect.getSelectionModel().select(0);
        }
        this.selectBrowser();
        this.remoteUrl.setText(remoteUrl);
        if (Strings.isNullOrEmpty(remoteUrl) && !Strings.isNullOrEmpty(aCurrentDriverPath)) {
            this.currentDriverPath = aCurrentDriverPath;
            this.driverText.setText(this.currentDriverPath);
            this.parentDir = new File(this.currentDriverPath).getParentFile().getAbsoluteFile();
        }
    }

    private void populate() {
        WebDriverFactory webdriverFactory = Context.getWebDriverFactory(this.selectedBrowser);
        String driverName = webdriverFactory.getDriverName();
        if (parentDir == null || !parentDir.exists()) {
            File driverParent = new File(Context.getBaseDirectory(), "exe/");
            if (!driverParent.exists()) {
                driverParent = Context.getBaseDirectory();
            }
            this.parentDir = driverParent.getAbsoluteFile();
        }
        this.remoteUrl.setText(Context.getRemoteUrl());
        if (Strings.isNullOrEmpty(remoteUrl.getText())) {
            this.driverText.setText(new File(parentDir, driverName).getAbsolutePath());
            if (Objects.equals(this.selectedBrowser, Context.getWebDriverFactory().targetBrowser())) {
                this.binaryText.setText(Context.getWebDriverFactory().getBinaryPath());
            } else {
                this.binaryText.setText(null);
            }
        }
        this.binaryText.setDisable(!webdriverFactory.isBinarySelectable());
        this.binarySearchButton.setDisable(binaryText.isDisable());
    }

}
