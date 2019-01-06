package com.sebuilder.interpreter.javafx.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Step;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StepEditController {

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

    public void init(Stage dialog, int stepIndex, String action) {
        this.dialog = dialog;
        this.stepIndex = stepIndex;
        this.action = action;
    }

    @FXML
    void initialize() {
        assert stepTypeSelect != null : "fx:id=\"stepTypeSelect\" was not injected: check your FXML file 'seleniumbuilderstepedit.fxml'.";
        assert stepEditGrid != null : "fx:id=\"stepEditGrid\" was not injected: check your FXML file 'seleniumbuilderstepedit.fxml'.";
        assert labelSelectType != null : "fx:id=\"labelSelectType\" was not injected: check your FXML file 'seleniumbuilderstepedit.fxml'.";
        stepTypeSelect.getItems().add("");
        stepTypeSelect.getItems().add("clickElement");
        stepTypeSelect.getItems().add("setElementText");
        stepTypeSelect.getItems().add("setElementSelected");
        stepTypeSelect.getItems().add("setElementNotSelected");
        stepTypeSelect.getItems().add("clearSelections");
        stepTypeSelect.getItems().add("sendKeysToElement");
        stepTypeSelect.getItems().add("submitElement");
        stepTypeSelect.getItems().add("fileDownload");
        stepTypeSelect.getItems().add("loop");
        stepTypeSelect.getItems().add("retry");
        stepTypeSelect.getItems().add("store");
        stepTypeSelect.getItems().add("pause");
        stepTypeSelect.getItems().add("storeElementPresent");
        stepTypeSelect.getItems().add("storeElementVisible");
        stepTypeSelect.getItems().add("storeElementEnable");
        stepTypeSelect.getItems().add("storeElementValue");
        stepTypeSelect.getItems().add("storeElementAttribute");
        stepTypeSelect.getItems().add("storeElementStyle");
        stepTypeSelect.getItems().add("storeCurrentUrl");
        stepTypeSelect.getItems().add("storeTitle");
        stepTypeSelect.getItems().add("storeText");
        stepTypeSelect.getItems().add("storeVariable");
        stepTypeSelect.getItems().add("storeCookieByName");
        stepTypeSelect.getItems().add("storeEval");
        stepTypeSelect.getItems().add("print");
        stepTypeSelect.getItems().add("printElementPresent");
        stepTypeSelect.getItems().add("printElementVisible");
        stepTypeSelect.getItems().add("printElementEnable");
        stepTypeSelect.getItems().add("printElementValue");
        stepTypeSelect.getItems().add("printElementAttribute");
        stepTypeSelect.getItems().add("printElementStyle");
        stepTypeSelect.getItems().add("printCurrentUrl");
        stepTypeSelect.getItems().add("printTitle");
        stepTypeSelect.getItems().add("printBodyText");
        stepTypeSelect.getItems().add("printPageSource");
        stepTypeSelect.getItems().add("printText");
        stepTypeSelect.getItems().add("printVariable");
        stepTypeSelect.getItems().add("printCookieByName");
        stepTypeSelect.getItems().add("printEval");
        stepTypeSelect.getItems().add("waitForElementPresent");
        stepTypeSelect.getItems().add("waitForElementVisible");
        stepTypeSelect.getItems().add("waitForElementEnable");
        stepTypeSelect.getItems().add("waitForElementValue");
        stepTypeSelect.getItems().add("waitForElementAttribute");
        stepTypeSelect.getItems().add("waitForElementStyle");
        stepTypeSelect.getItems().add("waitForCurrentUrl");
        stepTypeSelect.getItems().add("waitForTitle");
        stepTypeSelect.getItems().add("waitForText");
        stepTypeSelect.getItems().add("waitForTextPresent");
        stepTypeSelect.getItems().add("waitForVariable");
        stepTypeSelect.getItems().add("waitForAlertText");
        stepTypeSelect.getItems().add("waitForAlertPresent");
        stepTypeSelect.getItems().add("waitForCookieByName");
        stepTypeSelect.getItems().add("waitForCookiePresent");
        stepTypeSelect.getItems().add("waitForEval");
        stepTypeSelect.getItems().add("verifyElementPresent");
        stepTypeSelect.getItems().add("verifyElementVisible");
        stepTypeSelect.getItems().add("verifyElementEnable");
        stepTypeSelect.getItems().add("verifyElementValue");
        stepTypeSelect.getItems().add("verifyElementAttribute");
        stepTypeSelect.getItems().add("verifyElementStyle");
        stepTypeSelect.getItems().add("verifyCurrentUrl");
        stepTypeSelect.getItems().add("verifyTitle");
        stepTypeSelect.getItems().add("verifyText");
        stepTypeSelect.getItems().add("verifyTextPresent");
        stepTypeSelect.getItems().add("verifyVariable");
        stepTypeSelect.getItems().add("verifyAlertText");
        stepTypeSelect.getItems().add("verifyAlertPresent");
        stepTypeSelect.getItems().add("verifyCookieText");
        stepTypeSelect.getItems().add("verifyCookiePresent");
        stepTypeSelect.getItems().add("verifyEval");
        stepTypeSelect.getItems().add("assertElementPresent");
        stepTypeSelect.getItems().add("assertElementVisible");
        stepTypeSelect.getItems().add("assertElementEnable");
        stepTypeSelect.getItems().add("assertElementValue");
        stepTypeSelect.getItems().add("assertElementAttribute");
        stepTypeSelect.getItems().add("assertElementStyle");
        stepTypeSelect.getItems().add("assertTitle");
        stepTypeSelect.getItems().add("assertText");
        stepTypeSelect.getItems().add("assertTextPresent");
        stepTypeSelect.getItems().add("assertVariable");
        stepTypeSelect.getItems().add("assertAlertText");
        stepTypeSelect.getItems().add("assertAlertPresent");
        stepTypeSelect.getItems().add("assertCookieText");
        stepTypeSelect.getItems().add("assertCookiePresent");
        stepTypeSelect.getItems().add("assertEval");
        stepTypeSelect.getItems().add("acceptAlert");
        stepTypeSelect.getItems().add("answerAlert");
        stepTypeSelect.getItems().add("dismissAlert");
        stepTypeSelect.getItems().add("doubleClickElement");
        stepTypeSelect.getItems().add("clickAndHoldElement");
        stepTypeSelect.getItems().add("mouseOverElement");
        stepTypeSelect.getItems().add("releaseElement");
        stepTypeSelect.getItems().add("dragAndDropToElement");
        stepTypeSelect.getItems().add("addCookie");
        stepTypeSelect.getItems().add("deleteCookie");
        stepTypeSelect.getItems().add("refresh");
        stepTypeSelect.getItems().add("goBack");
        stepTypeSelect.getItems().add("goForward");
        stepTypeSelect.getItems().add("switchToDefaultContent");
        stepTypeSelect.getItems().add("switchToFrame");
        stepTypeSelect.getItems().add("switchToFrameByIndex");
        stepTypeSelect.getItems().add("switchToWindow");
        stepTypeSelect.getItems().add("switchToWindowByIndex");
        stepTypeSelect.getItems().add("switchToWindowByTitle");
        stepTypeSelect.getItems().add("saveScreenshot");
        stepTypeSelect.getItems().add("exportTemplate");
        stepTypeSelect.getItems().add("setWindowSize");
        stepTypeSelect.getItems().add("windowMaximize");
        stepTypeSelect.getItems().add("get");
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
    public void refleshView(RefreshStepEditViewEvent aEvent) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            Step step = aEvent.getStep();
            JSONObject json = step.toFullJSON();
            Iterator keys = json.keys();
            int row = 1;
            String typeName = this.resetStepType(json);
            row = addTextBox(json, row, "skip");
            row = addLocator(json, row, "locator", true);
            while (keys.hasNext()) {
                String key = keys.next().toString();
                if (key.equals("type") || key.equals("locator") || key.equals("skip")
                        || (key.equals("negated")
                        && !(typeName.startsWith("wait") || typeName.startsWith("verify") || typeName.startsWith("print") || typeName.startsWith("store")))) {
                    continue;
                }
                if (key.startsWith("locator")) {
                    row = this.addLocator(json, row, key, false);
                } else if (key.equals("negated") || key.equals("post")) {
                    row = addCheckBox(json, row, key);
                } else {
                    row = addTextBox(json, row, key);
                }
            }
            Button stepEditApply = createApplyButton();
            this.stepEditGrid.add(stepEditApply, 2, row);
            stepEditGrid.getScene().getWindow().sizeToScene();
        });
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

    private String resetStepType(JSONObject json) throws JSONException {
        String typeName = json.getString("type");
        typeName = typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
        if (typeName.equals(this.selectedStepType)) {
            return this.selectedStepType;
        }
        this.selectedStepType = typeName;
        this.stepTypeSelect.getSelectionModel().select(typeName);
        return typeName;
    }

    private int addLocator(JSONObject json, int row, String locator, boolean defaultLocator) throws JSONException {
        if (json.has(locator)) {
            Label stepEditLabel = new Label();
            stepEditLabel.setText(locator);
            ComboBox<String> select = resetLocatorSelect(json, locator, defaultLocator);
            this.stepEditGrid.add(stepEditLabel, 0, row);
            this.stepEditGrid.add(select, 1, row++);
            TextField text = resetLocatorText(json, locator, defaultLocator);
            Button button = new Button("find");
            button.setOnAction(ae -> {
                EventBus.publish(new ElementHighLightEvent(select.getSelectionModel().getSelectedItem(), text.getText()));
            });
            this.stepEditGrid.add(text, 1, row);
            this.stepEditGrid.add(button, 2, row++);
            HashMap<String, Node> input = Maps.newHashMap();
            input.put("type", select);
            input.put("value", text);
            this.locatorInputs.put(locator, input);
        } else if (defaultLocator) {
            this.locatorTypeSelect = null;
            this.locatorText = null;
            this.beforeLocatorType = null;
            this.beforeLocatorValue = null;
        }
        return row;
    }

    private ComboBox<String> resetLocatorSelect(JSONObject json, String locator, boolean defaultLocator) throws JSONException {
        ComboBox<String> select = new ComboBox<>();
        select.getItems().add("");
        select.getItems().add("id");
        select.getItems().add("name");
        select.getItems().add("css selector");
        select.getItems().add("xpath");
        select.getItems().add("link text");
        String type = json.getJSONObject(locator).getString("type");
        if (defaultLocator) {
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

    private TextField resetLocatorText(JSONObject json, String locator, boolean defaultLocator) throws JSONException {
        TextField text = new TextField();
        String value = json.getJSONObject(locator).getString("value");
        if (defaultLocator) {
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

    private int addCheckBox(JSONObject json, int row, String key) throws JSONException {
        Label label = new Label();
        label.setText(key);
        CheckBox checkbox = new CheckBox();
        checkbox.setSelected(Boolean.valueOf(json.getString(key)));
        this.stepEditGrid.add(label, 0, row);
        this.stepEditGrid.add(checkbox, 1, row++);
        this.inputs.put(key, checkbox);
        return row;
    }

    private int addTextBox(JSONObject json, int row, String key) throws JSONException {
        Label label = new Label();
        label.setText(key);
        TextField text = new TextField();
        text.setText(json.getString(key));
        this.stepEditGrid.add(label, 0, row);
        this.stepEditGrid.add(text, 1, row++);
        this.inputs.put(key, text);
        return row;
    }

    private Button createApplyButton() {
        Button result = new Button("apply");
        result.setOnAction(ae -> {
            ReportErrorEvent.publishIfExecuteThrowsException(() -> {
                JSONObject step = new JSONObject();
                step.put("type", this.selectedStepType);
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
                    JSONObject locator = new JSONObject();
                    step.put(input.getKey(), locator);
                    String type = ComboBox.class.cast(input.getValue().get("type")).getSelectionModel().getSelectedItem().toString();
                    locator.put("type", type);
                    String value = TextField.class.cast(input.getValue().get("value")).getText();
                    locator.put("value", value);
                }
                EventBus.publish(new StepEditEvent(this.action, this.stepIndex, step));
                this.dialog.close();
            });
        });
        return result;
    }

}