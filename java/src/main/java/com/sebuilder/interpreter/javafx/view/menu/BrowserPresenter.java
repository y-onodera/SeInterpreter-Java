package com.sebuilder.interpreter.javafx.view.menu;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.WebDriverFactory;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.replay.VariableView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BrowserPresenter {

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
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

    private InputData driverConfig;

    @FXML
    void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.init(Context.getRemoteUrl(), Context.getBrowser(), Context.getWebDriverFactory().getDriverPath());
            this.driverConfig = new InputData().builder().add(Context.getDriverConfig()).build()
                    .filter(it -> !it.getKey().equals("binary") && !it.getKey().equals(Context.REMOTE_URL_KEY));
        });
    }

    @FXML
    void selectBrowser() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final String browser = this.browserSelect.getSelectionModel().getSelectedItem();
            if (Objects.equals(this.selectedBrowser, browser)) {
                return;
            }
            this.selectedBrowser = this.browserSelect.getSelectionModel().getSelectedItem();
            this.populate();
        });
    }

    @FXML
    void setDriverPath() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.currentDriverPath = this.driverText.getText();
            if (!Strings.isNullOrEmpty(this.currentDriverPath)) {
                this.parentDir = new File(this.currentDriverPath).getParentFile().getAbsoluteFile();
            }
        });
    }

    @FXML
    void driverSearch() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.setInitialDirectory(this.parentDir);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("select driver.exe", "*.exe"));
            final Stage stage = new Stage();
            stage.initOwner(this.driverText.getScene().getWindow());
            final File file = fileChooser.showOpenDialog(stage);
            if (file != null && file.exists()) {
                this.parentDir = file.getParentFile().getAbsoluteFile();
                this.driverText.setText(file.getAbsolutePath());
            }
        });
    }

    @FXML
    void binarySearch() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.setInitialDirectory(this.parentDir);
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("select browser.exe", "*.exe"));
            final Stage stage = new Stage();
            stage.initOwner(this.binaryText.getScene().getWindow());
            final File file = fileChooser.showOpenDialog(stage);
            if (file != null && file.exists()) {
                this.binaryText.setText(file.getAbsolutePath());
            }
        });
    }

    @FXML
    void driverConfig() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            VariableView.builder()
                    .setTitle("env setting")
                    .setOnclick(result -> this.driverConfig = result)
                    .setTarget(this.driverConfig)
                    .setWindow(this.browserSelect.getScene().getWindow())
                    .build();
        });
    }

    @FXML
    void settingEdit() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final Map<String, String> newConfig = this.driverConfig.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey
                            , Map.Entry::getValue
                            , (e1, e2) -> e1
                            , HashMap::new));
            if (Context.getDriverConfig().containsKey("binary")) {
                newConfig.put("binary", Context.getDriverConfig().get("binary"));
            }
            if (Context.getDriverConfig().containsKey(Context.REMOTE_URL_KEY)) {
                newConfig.put(Context.REMOTE_URL_KEY, Context.getDriverConfig().get(Context.REMOTE_URL_KEY));
            }
            Context.getInstance().setDriverConfig(newConfig);
            this.application.browserSetting(this.selectedBrowser
                    , this.remoteUrl.getText()
                    , this.driverText.getText()
                    , this.binaryText.getText());
            this.driverText.getScene().getWindow().hide();
        });
    }

    private void init(final String remoteUrl, final String aSelectedBrowser, final String aCurrentDriverPath) {
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
        final WebDriverFactory webdriverFactory = Context.getWebDriverFactory(this.selectedBrowser);
        final String driverName = webdriverFactory.getDriverName() + ".exe";
        if (this.parentDir == null || !this.parentDir.exists()) {
            File driverParent = new File(Context.getBaseDirectory(), "exe/");
            if (!driverParent.exists()) {
                driverParent = Context.getBaseDirectory();
            }
            this.parentDir = driverParent.getAbsoluteFile();
        }
        this.remoteUrl.setText(Context.getRemoteUrl());
        if (Strings.isNullOrEmpty(this.remoteUrl.getText())) {
            if (new File(this.parentDir, driverName).exists()) {
                this.driverText.setText(new File(this.parentDir, driverName).getAbsolutePath());
            } else {
                this.driverText.setText("");
            }
            if (Objects.equals(this.selectedBrowser, Context.getWebDriverFactory().targetBrowser())) {
                this.binaryText.setText(Context.getWebDriverFactory().getBinaryPath());
            } else {
                this.binaryText.setText(null);
            }
        }
        this.binarySearchButton.setDisable(this.binaryText.isDisable());
    }

}
