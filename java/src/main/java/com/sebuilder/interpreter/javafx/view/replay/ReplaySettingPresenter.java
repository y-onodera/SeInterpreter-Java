package com.sebuilder.interpreter.javafx.view.replay;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.report.ReportFormat;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class ReplaySettingPresenter {

    @FXML
    public TextField maxWaitMsText;
    @FXML
    public TextField intervalMsText;
    @FXML
    private TextField datasourceText;

    @FXML
    public ComboBox<String> reportFormatSelect;

    @FXML
    void initialize() {
        this.maxWaitMsText.setText(String.valueOf(Context.getWaitForMaxMs()));
        this.intervalMsText.setText(String.valueOf(Context.getWaitForIntervalMs()));
        if (Context.getDataSourceDirectory().exists()) {
            this.datasourceText.setText(Context.getDataSourceDirectory().getAbsolutePath());
        } else {
            this.datasourceText.setText(Context.getBaseDirectory().getAbsolutePath());
        }
        this.reportFormatSelect.getItems().add(ReportFormat.EXTENT_REPORTS.getName());
        this.reportFormatSelect.getItems().add(ReportFormat.JUNIT.getName());
        this.reportFormatSelect.getSelectionModel().select(ReportFormat.valueOf(Context.getTestRunListenerFactory().toString()).getName());
    }

    @FXML
    void dataSourceSearch() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Directory Where DataSource File at");
        directoryChooser.setInitialDirectory(new File(this.datasourceText.getText()));
        final Stage stage = new Stage();
        stage.initOwner(this.datasourceText.getScene().getWindow());
        final File file = directoryChooser.showDialog(stage);
        if (file != null && file.exists()) {
            this.datasourceText.setText(file.getAbsolutePath());
        }
    }


    @FXML
    void settingEdit() {
        Context.getInstance().setDataSourceDirectory(this.datasourceText.getText())
                .setTestRunListenerFactory(ReportFormat.fromName(this.reportFormatSelect.getSelectionModel().getSelectedItem()))
                .setWaitForMaxMs(Integer.parseInt(this.maxWaitMsText.getText()))
                .setWaitForIntervalMs(Integer.parseInt(this.intervalMsText.getText()))
        ;
        this.datasourceText.getScene().getWindow().hide();
    }

}
