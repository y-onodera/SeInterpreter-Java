package com.sebuilder.interpreter.javafx.controller;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.replay.ElementHighLightEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultResetEvent;
import com.sebuilder.interpreter.javafx.event.replay.TemplateLoadEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;

public class ExportSettingController {
    private static final String DEFAULT_PARENT = "body";
    private static final String UNSPECIFIED = "unspecified";

    @FXML
    private GridPane exportSettingsGrid;

    @FXML
    private TextField textLocatorValue;

    @FXML
    private Button parentSearchButton;

    @FXML
    private Button exportButton;

    @FXML
    private ListView<String> listTargetTag;

    @FXML
    private ComboBox<String> selectParentLocator;

    @FXML
    private CheckBox checkWithDataSource;

    private String parentLocator;

    private String parentLocatorValue;

    private String targetTag;

    @FXML
    public void initialize() {
        assert this.textLocatorValue != null : "fx:id=\"textLocatorValue\" was not injected: check your FXML file 'exportsetting.fxml'.";
        assert this.parentSearchButton != null : "fx:id=\"parentSearchButton\" was not injected: check your FXML file 'exportsetting.fxml'.";
        assert this.exportButton != null : "fx:id=\"exportButton\" was not injected: check your FXML file 'exportsetting.fxml'.";
        assert this.listTargetTag != null : "fx:id=\"listTargetTag\" was not injected: check your FXML file 'exportsetting.fxml'.";
        assert this.selectParentLocator != null : "fx:id=\"selectParentLocator\" was not injected: check your FXML file 'exportsetting.fxml'.";
        assert checkWithDataSource != null : "fx:id=\"checkWithDataSource\" was not injected: check your FXML file 'exportsetting.fxml'.";
        this.selectParentLocator.getItems().clear();
        this.selectParentLocator.getItems().add(DEFAULT_PARENT);
        this.selectParentLocator.getItems().add("id");
        this.selectParentLocator.getItems().add("name");
        this.selectParentLocator.getItems().add("css selector");
        this.selectParentLocator.getItems().add("xpath");
        this.selectParentLocator.getItems().add("link text");
        this.selectParentLocator.getSelectionModel().select(DEFAULT_PARENT);
        this.removeLocatorValue();
        this.listTargetTag.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.listTargetTag.getItems().clear();
        this.listTargetTag.getItems().add("input");
        this.listTargetTag.getItems().add("select");
        this.listTargetTag.getItems().add("button");
        this.listTargetTag.getItems().add("a");
        this.listTargetTag.getItems().add("div");
        this.listTargetTag.getItems().add("span");
        this.listTargetTag.getSelectionModel().select("input");
        this.listTargetTag.getSelectionModel().select("select");
        this.listTargetTag.getSelectionModel().select("button");
        this.listTargetTag.getSelectionModel().select("a");
        this.checkWithDataSource.setSelected(true);
    }

    @FXML
    public void parentFind(ActionEvent event) {
        EventBus.publish(new ElementHighLightEvent(this.selectParentLocator.getSelectionModel().getSelectedItem(), this.textLocatorValue.getText()));
    }

    @FXML
    public void selectLocator(ActionEvent actionEvent) {
        String locator = this.selectParentLocator.getSelectionModel().getSelectedItem();
        if (DEFAULT_PARENT.equals(locator)) {
            if (this.exportSettingsGrid.getRowCount() == 3) {
                this.removeLocatorValue();
            }
            return;
        }
        this.exportSettingsGrid.add(this.textLocatorValue, 1, 1);
        this.exportSettingsGrid.add(this.parentSearchButton, 2, 1);
    }

    private void removeLocatorValue() {
        this.exportSettingsGrid.getChildren().remove(this.textLocatorValue);
        this.exportSettingsGrid.getChildren().remove(this.parentSearchButton);
    }

    @FXML
    public void execExport(ActionEvent event) {
        EventBus.publish(new StepResultResetEvent());
        String locator = this.selectParentLocator.getSelectionModel().getSelectedItem();
        String locatorValue = this.textLocatorValue.getText();
        List<String> targetTag = listTargetTag.getSelectionModel().getSelectedItems();
        if (DEFAULT_PARENT.equals(locator)) {
            EventBus.publish(new TemplateLoadEvent(new Locator("css selector", DEFAULT_PARENT), targetTag, this.checkWithDataSource.isSelected()));
        } else {
            EventBus.publish(new TemplateLoadEvent(new Locator(locator, locatorValue), targetTag, this.checkWithDataSource.isSelected()));
        }
        ((Stage) this.exportButton.getScene().getWindow()).close();
    }

}
