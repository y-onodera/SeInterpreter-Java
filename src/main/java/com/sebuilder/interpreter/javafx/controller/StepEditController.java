package com.sebuilder.interpreter.javafx.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.factory.StepTypeFactory;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.replay.ElementHighLightEvent;
import com.sebuilder.interpreter.javafx.event.script.StepAddEvent;
import com.sebuilder.interpreter.javafx.event.script.StepEditEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepEditViewEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class StepEditController {

    private static final String[] STEP_TYPES = {
            "ClickElement"
            , "SetElementText"
            , "SetElementSelected"
            , "SetElementNotSelected"
            , "ClearSelections"
            , "SendKeysToElement"
            , "SendKeysToPathElement"
            , "SubmitElement"
            , "FileDownload"
            , "ExecCmd"
            , "Pause"
            , "Loop"
            , "ifElementPresent"
            , "ifElementVisible"
            , "ifElementEnable"
            , "ifElementValue"
            , "ifElementAttribute"
            , "ifElementStyle"
            , "ifCurrentUrl"
            , "ifTitle"
            , "ifText"
            , "ifTextPresent"
            , "ifVariable"
            , "ifAlertText"
            , "ifAlertPresent"
            , "ifCookieByName"
            , "ifCookiePresent"
            , "ifEval"
            , "ifAntRun"
            , "ifDocumentReady"
            , "retryElementPresent"
            , "retryElementVisible"
            , "retryElementEnable"
            , "retryElementValue"
            , "retryElementAttribute"
            , "retryElementStyle"
            , "retryCurrentUrl"
            , "retryTitle"
            , "retryText"
            , "retryTextPresent"
            , "retryVariable"
            , "retryAlertText"
            , "retryAlertPresent"
            , "retryCookieByName"
            , "retryCookiePresent"
            , "retryEval"
            , "retryAntRun"
            , "retryDocumentReady"
            , "Store"
            , "storeElementPresent"
            , "storeElementVisible"
            , "storeElementEnable"
            , "storeElementValue"
            , "storeElementAttribute"
            , "storeElementStyle"
            , "storeCurrentUrl"
            , "storeTitle"
            , "storeText"
            , "storeVariable"
            , "storeCookieByName"
            , "storeEval"
            , "storeAntRun"
            , "Print"
            , "printElementPresent"
            , "printElementVisible"
            , "printElementEnable"
            , "printElementValue"
            , "printElementAttribute"
            , "printElementStyle"
            , "printCurrentUrl"
            , "printTitle"
            , "printBodyText"
            , "printPageSource"
            , "printText"
            , "printVariable"
            , "printCookieByName"
            , "printEval"
            , "printAntRun"
            , "waitForElementPresent"
            , "waitForElementVisible"
            , "waitForElementEnable"
            , "waitForElementValue"
            , "waitForElementAttribute"
            , "waitForElementStyle"
            , "waitForCurrentUrl"
            , "waitForTitle"
            , "waitForText"
            , "waitForTextPresent"
            , "waitForVariable"
            , "waitForAlertText"
            , "waitForAlertPresent"
            , "waitForCookieByName"
            , "waitForCookiePresent"
            , "waitForEval"
            , "waitForAntRun"
            , "waitForDocumentReady"
            , "verifyElementPresent"
            , "verifyElementVisible"
            , "verifyElementEnable"
            , "verifyElementValue"
            , "verifyElementAttribute"
            , "verifyElementStyle"
            , "verifyCurrentUrl"
            , "verifyTitle"
            , "verifyText"
            , "verifyTextPresent"
            , "verifyVariable"
            , "verifyAlertText"
            , "verifyAlertPresent"
            , "verifyCookieByName"
            , "verifyCookiePresent"
            , "verifyEval"
            , "verifyAntRun"
            , "verifyDocumentReady"
            , "assertElementPresent"
            , "assertElementVisible"
            , "assertElementEnable"
            , "assertElementValue"
            , "assertElementAttribute"
            , "assertElementStyle"
            , "assertTitle"
            , "assertText"
            , "assertTextPresent"
            , "assertVariable"
            , "assertAlertText"
            , "assertAlertPresent"
            , "assertCookieByName"
            , "assertCookiePresent"
            , "assertEval"
            , "assertAntRun"
            , "assertDocumentReady"
            , "AcceptAlert"
            , "AnswerAlert"
            , "DismissAlert"
            , "DoubleClickElement"
            , "ClickAndHoldElement"
            , "MouseOverElement"
            , "ReleaseElement"
            , "DragAndDropToElement"
            , "AddCookie"
            , "DeleteCookie"
            , "SwitchToDefaultContent"
            , "SwitchToFrame"
            , "SwitchToFrameByIndex"
            , "SwitchToWindow"
            , "SwitchToWindowByIndex"
            , "SwitchToWindowByTitle"
            , "SaveScreenshot"
            , "ExportTemplate"
            , "SetWindowSize"
            , "WindowMaximize"
            , "Get"
            , "Refresh"
            , "GoBack"
            , "GoForward"
    };
    @FXML
    private GridPane stepEditGrid;

    @FXML
    private ComboBox<String> stepTypeSelect;

    @FXML
    private Label labelSelectType;

    private String selectedStepType;

    private ComboBox<String> locatorTypeSelect;

    private TextField locatorText;

    private String beforeLocatorType;

    private String beforeLocatorValue;

    private Map<String, Node> inputs = Maps.newHashMap();

    private Map<String, Map<String, Node>> locatorInputs = Maps.newHashMap();

    private Stage dialog;

    private int stepIndex;

    private String action;

    private StepTypeFactory stepTypeFactory = new StepTypeFactory();

    public void init(Stage dialog, int stepIndex, String action) {
        this.dialog = dialog;
        this.stepIndex = stepIndex;
        this.action = action;
    }

    @FXML
    void initialize() {
        assert stepTypeSelect != null : "fx:id=\"stepTypeSelect\" was not injected: check your FXML file 'stepedit.fxml'.";
        assert stepEditGrid != null : "fx:id=\"stepEditGrid\" was not injected: check your FXML file 'stepedit.fxml'.";
        assert labelSelectType != null : "fx:id=\"labelSelectType\" was not injected: check your FXML file 'stepedit.fxml'.";
        stepTypeSelect.getItems().add("");
        for (String stepType : this.stepTypes()) {
            stepTypeSelect.getItems().add(stepType);
        }
        stepTypeSelect.getSelectionModel().select(0);
        this.selectedStepType = this.stepTypeSelect.getSelectionModel().getSelectedItem();
        EventBus.registSubscriber(this);
    }

    @FXML
    void selectType(ActionEvent event) {
        String stepType = stepTypeSelect.getSelectionModel().getSelectedItem();
        if (this.selectedStepType.equals(stepType)) {
            return;
        }
        this.selectedStepType = this.stepTypeSelect.getSelectionModel().getSelectedItem();
        this.clearInputFields();
        EventBus.publish(new StepAddEvent(stepType));
    }

    @Subscribe
    public void refreshView(RefreshStepEditViewEvent aEvent) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            Step step = aEvent.getStep().withAllParam();
            int row = 1;
            String typeName = this.resetStepType(step);
            row = this.addTextBox(step, row, "skip");
            row = this.addLocator(step, row, "locator");
            row = this.constructStringParamView(step, row, typeName);
            row = this.constructLocatorParamView(step, row);
            Button stepEditApply = createApplyButton();
            this.stepEditGrid.add(stepEditApply, 2, row);
            stepEditGrid.getScene().getWindow().sizeToScene();
        });
    }

    private StepType getStepTypeOfName(String selectedStepType) {
        return this.stepTypeFactory.getStepTypeOfName(selectedStepType);
    }

    private String[] stepTypes() {
        return STEP_TYPES;
    }

    private boolean isDefaultLocator(String key) {
        return key.equals("locator");
    }

    private int constructLocatorParamView(Step step, int row) throws JSONException {
        for (String key : step.locatorKeys()) {
            if (this.isDefaultLocator(key)) {
                continue;
            }
            row = this.addLocator(step, row, key);
        }
        return row;
    }

    private int constructStringParamView(Step step, int row, String typeName) throws JSONException {
        for (String key : step.paramKeys()) {
            if (key.equals("type") || key.equals("skip")
                    || (key.equals("negated") && !hasGetterType(typeName))) {
                continue;
            }
            if (key.equals("negated") || key.equals("post")) {
                row = addCheckBox(step, row, key);
            } else {
                row = addTextBox(step, row, key);
            }
        }
        return row;
    }

    private boolean hasGetterType(String typeName) {
        return typeName.startsWith("wait")
                || typeName.startsWith("assert")
                || typeName.startsWith("verify")
                || typeName.startsWith("if")
                || typeName.startsWith("retry")
                || typeName.startsWith("print")
                || typeName.startsWith("store");
    }

    private void clearInputFields() {
        this.backupBeforeLocator();
        this.inputs.clear();
        this.locatorInputs.clear();
        for (Node node : new ArrayList<>(this.stepEditGrid.getChildren())) {
            if (!this.stepTypeSelect.equals(node) && !this.labelSelectType.equals(node)) {
                this.stepEditGrid.getChildren().remove(node);
            }
        }
    }

    private void backupBeforeLocator() {
        if (this.locatorTypeSelect != null && this.stepEditGrid.getChildren().contains(this.locatorTypeSelect)) {
            this.beforeLocatorType = this.locatorTypeSelect.getSelectionModel().getSelectedItem();
            this.beforeLocatorValue = this.locatorText.getText();
        } else {
            this.beforeLocatorType = null;
            this.beforeLocatorValue = null;
        }
    }

    private String resetStepType(Step step) throws JSONException {
        String typeName = step.getType().getStepTypeName();
        if (typeName.equals(this.selectedStepType)) {
            return this.selectedStepType;
        }
        this.selectedStepType = typeName;
        this.stepTypeSelect.getSelectionModel().select(typeName);
        return typeName;
    }

    private int addLocator(Step step, int row, String locator) throws JSONException {
        if (step.locatorContains(locator)) {
            Label stepEditLabel = new Label();
            stepEditLabel.setText(locator);
            ComboBox<String> select = resetLocatorSelect(step, locator);
            this.stepEditGrid.add(stepEditLabel, 0, row);
            this.stepEditGrid.add(select, 1, row++);
            TextField text = resetLocatorText(step, locator);
            Button button = new Button("find");
            button.setOnAction(ae -> {
                EventBus.publish(new ElementHighLightEvent(select.getSelectionModel().getSelectedItem(), text.getText()));
            });
            this.stepEditGrid.add(text, 1, row);
            this.stepEditGrid.add(button, 2, row++);
            Map<String, Node> input = Maps.newHashMap();
            input.put("type", select);
            input.put("value", text);
            this.locatorInputs.put(locator, input);
        } else if (this.isDefaultLocator(locator)) {
            this.locatorTypeSelect = null;
            this.locatorText = null;
            this.beforeLocatorType = null;
            this.beforeLocatorValue = null;
        }
        return row;
    }

    private ComboBox<String> resetLocatorSelect(Step step, String locator) throws JSONException {
        ComboBox<String> select = new ComboBox<>();
        select.getItems().add("");
        select.getItems().add("id");
        select.getItems().add("name");
        select.getItems().add("css selector");
        select.getItems().add("xpath");
        select.getItems().add("link text");
        String type = step.getLocator(locator).type.toString();
        if (this.isDefaultLocator(locator)) {
            locatorTypeSelect = select;
            if (!type.equals("")) {
                this.beforeLocatorType = type;
            }
            if (this.beforeLocatorType != null) {
                locatorTypeSelect.getSelectionModel().select(this.beforeLocatorType);
            } else {
                locatorTypeSelect.getSelectionModel().select("");
            }
        } else {
            select.getSelectionModel().select(type);
        }
        return select;
    }

    private TextField resetLocatorText(Step step, String locator) throws JSONException {
        TextField text = new TextField();
        String value = step.getLocator(locator).value;
        if (this.isDefaultLocator(locator)) {
            this.locatorText = text;
            if (!value.equals("")) {
                this.beforeLocatorValue = value;
            }
            if (this.beforeLocatorValue != null) {
                this.locatorText.setText(this.beforeLocatorValue);
            } else {
                text.setText(value);
            }
        } else {
            text.setText(value);
        }
        return text;
    }

    private int addCheckBox(Step step, int row, String key) throws JSONException {
        Label label = new Label();
        label.setText(key);
        CheckBox checkbox = new CheckBox();
        checkbox.setSelected(Boolean.valueOf(step.getParam(key)));
        this.stepEditGrid.add(label, 0, row);
        this.stepEditGrid.add(checkbox, 1, row++);
        this.inputs.put(key, checkbox);
        return row;
    }

    private int addTextBox(Step step, int row, String key) throws JSONException {
        Label label = new Label();
        label.setText(key);
        TextField text = new TextField();
        text.setText(step.getParam(key));
        this.stepEditGrid.add(label, 0, row);
        this.stepEditGrid.add(text, 1, row++);
        this.inputs.put(key, text);
        return row;
    }

    private Button createApplyButton() {
        Button result = new Button("apply");
        result.setOnAction(ae -> {
            ReportErrorEvent.publishIfExecuteThrowsException(() -> {
                StepBuilder step = new StepBuilder(this.getStepTypeOfName(this.selectedStepType));
                for (Map.Entry<String, Node> input : this.inputs.entrySet()) {
                    if (input.getValue() instanceof TextField) {
                        TextField text = TextField.class.cast(input.getValue());
                        if (!Strings.isNullOrEmpty(text.getText())) {
                            step.put(input.getKey(), text.getText());
                        }
                    } else if (input.getValue() instanceof CheckBox) {
                        CheckBox check = CheckBox.class.cast(input.getValue());
                        if (check.isSelected()) {
                            step.put(input.getKey(), "true");
                        }
                    }
                }
                for (Map.Entry<String, Map<String, Node>> input : this.locatorInputs.entrySet()) {
                    String type = ComboBox.class.cast(input.getValue().get("type")).getSelectionModel().getSelectedItem().toString();
                    if (!Strings.isNullOrEmpty(type)) {
                        String value = TextField.class.cast(input.getValue().get("value")).getText();
                        step.put(input.getKey(), new Locator(type, value));
                    }
                }
                EventBus.publish(new StepEditEvent(this.action, this.stepIndex, step.build()));
                this.dialog.close();
            });
        });
        return result;
    }
}