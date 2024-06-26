package com.sebuilder.interpreter.javafx.view.suite;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.tbee.javafx.scene.layout.MigPane;

import javax.inject.Inject;
import java.util.List;

public class TemplatePresenter {

    private static final String DEFAULT_PARENT = "body";

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private MigPane exportSettingsGrid;

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

    @FXML
    void initialize() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
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
        });
    }

    @FXML
    void parentFind() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() ->
                this.application.highLightElement(this.selectParentLocator.getSelectionModel().getSelectedItem(), this.textLocatorValue.getText())
        );
    }

    @FXML
    void selectLocator() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final String locator = this.selectParentLocator.getSelectionModel().getSelectedItem();
            if (this.exportSettingsGrid.getChildren().contains(this.textLocatorValue)) {
                if (DEFAULT_PARENT.equals(locator)) {
                    this.removeLocatorValue();
                }
            } else {
                this.exportSettingsGrid.add(this.textLocatorValue, "cell 1 1,grow");
                this.exportSettingsGrid.add(this.parentSearchButton, "cell 2 1");
            }
        });
    }

    @FXML
    void execExport() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final String locator = this.selectParentLocator.getSelectionModel().getSelectedItem();
            final String locatorValue = this.textLocatorValue.getText();
            final List<String> targetTag = this.listTargetTag.getSelectionModel().getSelectedItems();
            final TestCase template;
            if (DEFAULT_PARENT.equals(locator)) {
                template = this.application.exportTemplate(new Locator("css selector", DEFAULT_PARENT), targetTag, this.checkWithDataSource.isSelected());
            } else {
                template = this.application.exportTemplate(new Locator(locator, locatorValue), targetTag, this.checkWithDataSource.isSelected());
            }
            this.application.addScript(template);
            ((Stage) this.exportButton.getScene().getWindow()).close();
        });
    }

    private void removeLocatorValue() {
        this.exportSettingsGrid.getChildren().remove(this.textLocatorValue);
        this.exportSettingsGrid.getChildren().remove(this.parentSearchButton);
    }

}
