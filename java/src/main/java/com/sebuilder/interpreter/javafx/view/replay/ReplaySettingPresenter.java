package com.sebuilder.interpreter.javafx.view.replay;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.report.ReportFormat;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.Arrays;

public class ReplaySettingPresenter {

    @FXML
    private TextField maxWaitMsText;
    @FXML
    private TextField intervalMsText;
    @FXML
    private TextField datasourceText;

    @FXML
    private ComboBox<String> reportFormatSelect;

    @FXML
    private ComboBox<String> reportPrefixSelect;

    private InputData envProperties;

    @FXML
    void initialize() {
        this.maxWaitMsText.setText(String.valueOf(Context.getWaitForMaxMs()));
        this.intervalMsText.setText(String.valueOf(Context.getWaitForIntervalMs()));
        if (Context.getDataSourceDirectory().exists()) {
            this.datasourceText.setText(Context.getDataSourceDirectory().getAbsolutePath());
        } else {
            this.datasourceText.setText(Context.getBaseDirectory().getAbsolutePath());
        }
        Arrays.stream(ReportFormat.values()).forEach(it -> this.reportFormatSelect.getItems().add(it.getName()));
        this.reportFormatSelect.getSelectionModel().select(ReportFormat.valueOf(Context.getTestRunListenerFactory().toString()).getName());
        Arrays.stream(Context.ReportPrefix.values()).forEach(it ->
                this.reportPrefixSelect.getItems().add(it.getName())
        );
        this.reportPrefixSelect.getSelectionModel().select(Context.getReportPrefix().getName());
        this.envProperties = Context.settings()
                .filter(entry -> entry.getKey().startsWith("env."))
                .replaceKey(entry -> entry.getKey().replace("env.", ""));
    }

    @FXML
    void dataSourceSearch() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Directory Where DataSource File at");
        directoryChooser.setInitialDirectory(new File(this.datasourceText.getText()));
        final Stage stage = new Stage();
        stage.initOwner(this.currentWindow());
        final File file = directoryChooser.showDialog(stage);
        if (file != null && file.exists()) {
            this.datasourceText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void envSetting() {
        VariableView.builder()
                .setTitle("env setting")
                .setOnclick(result -> this.envProperties = result)
                .setTarget(this.envProperties)
                .setWindow(this.currentWindow())
                .build();
    }

    @FXML
    void settingEdit() {
        Context.getInstance()
                .setWaitForMaxMs(Integer.parseInt(this.maxWaitMsText.getText()))
                .setWaitForIntervalMs(Integer.parseInt(this.intervalMsText.getText()))
                .setDataSourceDirectory(this.datasourceText.getText())
                .setTestRunListenerFactory(ReportFormat.fromName(this.reportFormatSelect.getSelectionModel().getSelectedItem()))
                .setReportPrefix(Context.ReportPrefix.fromName(this.reportPrefixSelect.getSelectionModel().getSelectedItem()))
                .setEnvProperties(this.envProperties)
        ;
        this.currentWindow().hide();
    }

    private Window currentWindow() {
        return this.datasourceText.getScene().getWindow();
    }
}
