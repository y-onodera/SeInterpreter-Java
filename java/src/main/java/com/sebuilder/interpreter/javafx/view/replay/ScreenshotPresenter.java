package com.sebuilder.interpreter.javafx.view.replay;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.HasFileChooser;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Window;
import org.tbee.javafx.scene.layout.fxml.MigPane;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ScreenshotPresenter implements HasFileChooser {

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private Button remove;

    @FXML
    private MigPane stepEditGrid;

    @FXML
    private MenuItem menuSaveFile;

    @FXML
    private ComboBox<String> templateSelect;

    @FXML
    private Label labelTemplateSelect;

    private final Map<String, Step> templates = new LinkedHashMap<>();

    private final Map<String, Node> inputs = new HashMap<>();

    private final Map<String, ComboBox<String>> locatorTypes = new HashMap<>();

    private final Map<String, TextField> locatorValues = new HashMap<>();

    private String currentSelected;

    void populate(final String selected) {
        this.templates.clear();
        this.templateSelect.getItems().clear();
        this.currentSelected = null;
        this.templates.putAll(this.application.takeScreenshotTemplate());
        this.templateSelect.getItems().setAll(this.templates.keySet());
        if (selected == null) {
            this.templateSelect.getSelectionModel().select(0);
        } else {
            this.templateSelect.getSelectionModel().select(selected);
        }
        this.menuSaveFile.disableProperty().bind(Bindings.size(this.templateSelect.getItems()).lessThan(2));
        this.remove.disableProperty().bind(this.templateSelect.getSelectionModel().selectedIndexProperty().isEqualTo(0));
    }

    @FXML
    void loadTemplate() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final File file = this.openDialog("Open Resource File", "json format (*.json)", "*.json");
            if (file != null) {
                this.application.reloadScreenshotTemplate(file);
                this.populate(null);
            }
        });
    }

    @FXML
    void addTemplate() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final TextInputDialog dialog = new TextInputDialog();
            dialog.initOwner(this.currentWindow());
            dialog.setTitle("new template name");
            dialog.setHeaderText(null);
            dialog.setGraphic(null);
            dialog.getDialogPane().lookupButton(ButtonType.OK)
                    .disableProperty()
                    .bind(dialog.getEditor().textProperty().isEmpty());
            dialog.showAndWait().ifPresent(response -> {
                this.application.addScreenshotTemplates(this.inputToStep().put("displayName", response).build());
                this.populate(response);
            });
        });
    }

    @FXML
    void removeTemplate() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.application.removeScreenshotTemplate(this.templateSelect.getSelectionModel().getSelectedItem());
            this.populate(null);
        });
    }

    @FXML
    void saveTemplate() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final File file = this.saveDialog("Save TestCase File", "json format (*.json)", "*.json");
            if (file != null) {
                this.application.saveScreenshotTemplate(file);
            }
        });
    }

    @FXML
    void selectTemplate() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final String selected = this.templateSelect.getSelectionModel().getSelectedItem();
            if (Objects.equals(this.currentSelected, selected)) {
                return;
            }
            final Step stepWithAllParam = this.templates.get(selected);
            if (stepWithAllParam != null) {
                this.currentSelected = selected;
                this.inputs.clear();
                this.locatorTypes.clear();
                this.locatorValues.clear();
                this.stepEditGrid.getChildren().removeIf(node -> !this.templateSelect.equals(node) && !this.labelTemplateSelect.equals(node));
                this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
                    int row = 0;
                    row = this.addLocator(stepWithAllParam, row, "locator");
                    row = this.addLocator(stepWithAllParam, row, "locatorHeader");
                    row = this.addTextBox(stepWithAllParam, row, "file");
                    this.addTextBox(stepWithAllParam, row, "scroll");
                    this.stepEditGrid.getScene().getWindow().sizeToScene();
                });
            }
        });
    }

    @FXML
    void takeScreenshot() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final ClipboardContent newContent = new ClipboardContent();
            final File screenshot = this.application.takeScreenShot(this.inputToStep());
            newContent.putImage(new Image(new FileInputStream(screenshot)));
            Clipboard.getSystemClipboard().setContent(newContent);
            SuccessDialog.show(String.format("copy clipboard and save to:%s", screenshot.getAbsolutePath()));
        });
    }

    @Override
    public Window currentWindow() {
        return this.templateSelect.getScene().getWindow();
    }

    private StepBuilder inputToStep() {
        final StepBuilder step = Context.createStepBuilder("saveScreenshot");
        this.inputs.forEach((key, value) -> {
            if (value instanceof TextField text) {
                if (!Strings.isNullOrEmpty(text.getText())) {
                    step.put(key, text.getText());
                }
            }
        });
        this.locatorTypes.forEach((key, value1) -> {
            final String type = value1.getSelectionModel().getSelectedItem();
            if (!Strings.isNullOrEmpty(type)) {
                final String value = this.locatorValues.get(key).getText();
                step.put(key, new Locator(type, value));
            }
        });
        return step;
    }

    private int addLocator(final Step step, int row, final String locator) {
        final Label stepEditLabel = new Label();
        stepEditLabel.setText(locator);
        final ComboBox<String> select = this.resetLocatorSelect(step, locator);
        this.stepEditGrid.add(stepEditLabel, "cell 0 " + row);
        this.stepEditGrid.add(select, "cell 1 " + row++);
        final TextField text = this.resetLocatorText(step, locator);
        final Button button = new Button("find");
        button.setOnAction(ae -> this.application.highLightElement(select.getSelectionModel().getSelectedItem(), text.getText()));
        this.stepEditGrid.add(text, "width 150,cell 1 " + row);
        this.stepEditGrid.add(button, "cell 2 " + row++);
        this.locatorTypes.put(locator, select);
        this.locatorValues.put(locator, text);
        return row;
    }

    private ComboBox<String> resetLocatorSelect(final Step step, final String locator) {
        final ComboBox<String> select = new ComboBox<>();
        select.getItems().add("");
        select.getItems().add("id");
        select.getItems().add("name");
        select.getItems().add("css selector");
        select.getItems().add("xpath");
        select.getItems().add("link text");
        String type = "";
        if (step.locatorContains(locator)) {
            type = step.getLocator(locator).type();
        }
        select.getSelectionModel().select(type);
        return select;
    }

    private TextField resetLocatorText(final Step step, final String locator) {
        final TextField textField = new TextField();
        if (step.locatorContains(locator)) {
            textField.setText(step.getLocator(locator).value());
        }
        return textField;
    }

    private int addTextBox(final Step step, final int row, final String key) {
        return this.addTextBox(key, row, step.getParam(key), this.inputs);
    }

    private int addTextBox(final String key, int row, final String value, final Map<String, Node> inputMap) {
        final Label label = new Label();
        label.setText(key);
        final TextField text = new TextField();
        text.setText(value);
        this.stepEditGrid.add(label, "cell 0 " + row);
        this.stepEditGrid.add(text, "width 150,cell 1 " + row++);
        inputMap.put(key, text);
        return row;
    }

}
