package com.sebuilder.interpreter.javafx.view.menu;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.WebDriverFactory;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.HasFileChooser;
import com.sebuilder.interpreter.javafx.view.replay.VariableView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BrowserPresenter implements HasFileChooser {

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private ComboBox<String> browserSelect;
    @FXML
    private ComboBox<String> browserVersion;
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
            this.browserSelect.getItems().add("Chrome");
            this.browserSelect.getItems().add("Firefox");
            this.browserSelect.getItems().add("InternetExplorer");
            this.browserSelect.getItems().add("Edge");
            if (!Strings.isNullOrEmpty(Context.getBrowser())) {
                this.browserSelect.getSelectionModel().select(Context.getBrowser());
            } else {
                this.browserSelect.getSelectionModel().select(0);
            }
            this.selectBrowser();
            this.browserVersion.setEditable(true);
            this.browserVersion.getItems().add("");
            this.browserVersion.getItems().add("stable");
            this.browserVersion.getItems().add("beta");
            this.browserVersion.getItems().add("dev");
            this.browserVersion.getItems().add("canary");
            this.browserVersion.getItems().add("nightly");
            this.browserVersion.setValue(Context.getBrowserVersion());
            this.browserVersion.getSelectionModel().selectedItemProperty().addListener((observed, oldValue, newValue) ->
                    Optional.ofNullable(newValue).ifPresent(it -> {
                        if (!Objects.equals(oldValue, newValue) && !it.isEmpty()) {
                            this.driverText.setText(null);
                            this.binaryText.setText(null);
                        }
                    }));
        });
        this.remoteUrl.setText(Context.getRemoteUrl());
        if (Strings.isNullOrEmpty(Context.getRemoteUrl()) && !Strings.isNullOrEmpty(Context.getWebDriverFactory().getDriverPath())) {
            this.currentDriverPath = Context.getWebDriverFactory().getDriverPath();
            this.driverText.setText(this.currentDriverPath);
            this.parentDir = new File(this.currentDriverPath).getParentFile().getAbsoluteFile();
        }
        this.driverConfig = new InputData().builder().add(Context.getDriverConfig()).build()
                .filter(it -> !it.getKey().equals(Context.BROWSER_BINARY_KEY)
                        && !it.getKey().equals(Context.REMOTE_URL_KEY)
                );
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
            final File file = this.openDialog("Open Resource File", "select driver.exe", "*.exe");
            if (file != null && file.exists()) {
                this.parentDir = file.getParentFile().getAbsoluteFile();
                this.driverText.setText(file.getAbsolutePath());
            }
        });
    }

    @FXML
    void binarySearch() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final File file = this.openDialog("Open Resource File", "select browser.exe", "*.exe");
            if (file != null && file.exists()) {
                this.binaryText.setText(file.getAbsolutePath());
            }
        });
    }

    @FXML
    void driverConfig() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() ->
                VariableView.builder()
                        .setTitle("env setting")
                        .setOnclick(result -> this.driverConfig = result)
                        .setTarget(this.driverConfig)
                        .setWindow(this.browserSelect.getScene().getWindow())
                        .build());
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
            if (Context.getDriverConfig().containsKey(Context.REMOTE_URL_KEY)) {
                newConfig.put(Context.REMOTE_URL_KEY, Context.getDriverConfig().get(Context.REMOTE_URL_KEY));
            }
            Context.getInstance().setDriverConfig(newConfig);
            this.application.browserSetting(this.selectedBrowser
                    , this.browserVersion.getValue()
                    , this.remoteUrl.getText()
                    , this.driverText.getText()
                    , this.binaryText.getText());
            this.driverText.getScene().getWindow().hide();
        });
    }

    @Override
    public Window currentWindow() {
        return this.browserSelect.getScene().getWindow();
    }

    @Override
    public File getBaseDirectory() {
        return this.parentDir;
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
