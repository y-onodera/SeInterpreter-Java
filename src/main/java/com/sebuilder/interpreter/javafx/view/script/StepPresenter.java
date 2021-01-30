package com.sebuilder.interpreter.javafx.view.script;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

public class StepPresenter {

    @Inject
    private SeInterpreterApplication apprication;

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
            , "ifCmd"
            , "ifEval"
            , "ifAntRun"
            , "ifDocumentReady"
            , "ifWindowHandle"
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
            , "retryCmd"
            , "retryEval"
            , "retryAntRun"
            , "retryDocumentReady"
            , "retryWindowHandle"
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
            , "storePointX"
            , "storePointY"
            , "storeClientHeight"
            , "storeClientWidth"
            , "storeScrollableHeight"
            , "storeScrollableWidth"
            , "storeWindowHeight"
            , "storeWindowWidth"
            , "storeVariable"
            , "storeCookieByName"
            , "storeCmd"
            , "storeEval"
            , "storeAntRun"
            , "storeWindowHandle"
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
            , "printPointX"
            , "printPointY"
            , "printClientHeight"
            , "printClientWidth"
            , "printScrollableHeight"
            , "printScrollableWidth"
            , "printWindowHeight"
            , "printWindowWidth"
            , "printVariable"
            , "printCookieByName"
            , "printCmd"
            , "printEval"
            , "printAntRun"
            , "printWindowHandle"
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
            , "waitForCmd"
            , "waitForEval"
            , "waitForAntRun"
            , "waitForDocumentReady"
            , "waitForWindowHandle"
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
            , "verifyCmd"
            , "verifyEval"
            , "verifyAntRun"
            , "verifyDocumentReady"
            , "verifyWindowHandle"
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
            , "assertCmd"
            , "assertEval"
            , "assertAntRun"
            , "assertDocumentReady"
            , "assertWindowHandle"
            , "AcceptAlert"
            , "AnswerAlert"
            , "DismissAlert"
            , "ClickElementAt"
            , "DoubleClickElement"
            , "DoubleClickElementAt"
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
            , "ScrollUp"
            , "ScrollDown"
            , "ScrollLeft"
            , "ScrollRight"
            , "ScrollHorizontally"
            , "ScrollVertically"
            , "KeyUp"
            , "KeyDown"
            , "KeyTyping"
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

    private StepView.Action action;

    public void init(Stage dialog, int stepIndex, StepView.Action action) {
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
    }

    @FXML
    void selectType() {
        String stepType = stepTypeSelect.getSelectionModel().getSelectedItem();
        if (this.selectedStepType.equals(stepType)) {
            return;
        }
        this.selectedStepType = this.stepTypeSelect.getSelectionModel().getSelectedItem();
        this.clearInputFields();
        this.refreshView(this.apprication.createStep(stepType));
    }

    void refreshView(Step step) {
        Step stepWithAllParam = step.withAllParam();
        this.apprication.executeAndLoggingCaseWhenThrowException(() -> {
            int row = 1;
            String typeName = this.resetStepType(stepWithAllParam);
            row = this.addTextBox(stepWithAllParam, row, "skip");
            row = this.addLocator(stepWithAllParam, row, "locator");
            row = this.constructStringParamView(stepWithAllParam, row, typeName);
            row = this.constructLocatorParamView(stepWithAllParam, row);
            Button stepEditApply = createApplyButton();
            this.stepEditGrid.add(stepEditApply, 2, row);
            stepEditGrid.getScene().getWindow().sizeToScene();
        });
    }

    private String[] stepTypes() {
        return STEP_TYPES;
    }

    private boolean isDefaultLocator(String key) {
        return key.equals("locator");
    }

    private int constructLocatorParamView(Step step, int row) {
        for (String key : step.locatorKeys()) {
            if (this.isDefaultLocator(key)) {
                continue;
            }
            row = this.addLocator(step, row, key);
        }
        return row;
    }

    private int constructStringParamView(Step step, int row, String typeName) {
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
        this.stepEditGrid.getChildren().removeIf(node -> !this.stepTypeSelect.equals(node) && !this.labelSelectType.equals(node));
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

    private String resetStepType(Step step) {
        String typeName = step.getType().getStepTypeName();
        if (typeName.equals(this.selectedStepType)) {
            return this.selectedStepType;
        }
        this.selectedStepType = typeName;
        this.stepTypeSelect.getSelectionModel().select(typeName);
        return typeName;
    }

    private int addLocator(Step step, int row, String locator) {
        if (step.locatorContains(locator)) {
            Label stepEditLabel = new Label();
            stepEditLabel.setText(locator);
            ComboBox<String> select = resetLocatorSelect(step, locator);
            this.stepEditGrid.add(stepEditLabel, 0, row);
            this.stepEditGrid.add(select, 1, row++);
            TextField text = resetLocatorText(step, locator);
            Button button = new Button("find");
            button.setOnAction(ae -> this.apprication.highLightElement(select.getSelectionModel().getSelectedItem(), text.getText()));
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

    private ComboBox<String> resetLocatorSelect(Step step, String locator) {
        ComboBox<String> select = new ComboBox<>();
        select.getItems().add("");
        select.getItems().add("id");
        select.getItems().add("name");
        select.getItems().add("css selector");
        select.getItems().add("xpath");
        select.getItems().add("link text");
        Locator current = step.getLocator(locator);
        String type = "";
        if (!Strings.isNullOrEmpty(current.value)) {
            type = current.type.toString();
        }
        if (this.isDefaultLocator(locator)) {
            locatorTypeSelect = select;
            if (!type.equals("")) {
                this.beforeLocatorType = type;
            }
            locatorTypeSelect.getSelectionModel().select(Objects.requireNonNullElse(this.beforeLocatorType, ""));
        } else {
            select.getSelectionModel().select(type);
        }
        return select;
    }

    private TextField resetLocatorText(Step step, String locator) {
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

    private int addCheckBox(Step step, int row, String key) {
        Label label = new Label();
        label.setText(key);
        CheckBox checkbox = new CheckBox();
        checkbox.setSelected(Boolean.parseBoolean(step.getParam(key)));
        this.stepEditGrid.add(label, 0, row);
        this.stepEditGrid.add(checkbox, 1, row++);
        this.inputs.put(key, checkbox);
        return row;
    }

    private int addTextBox(Step step, int row, String key) {
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
            this.apprication.executeAndLoggingCaseWhenThrowException(() -> {
                StepBuilder step = new StepBuilder(this.apprication.getStepTypeOfName(this.selectedStepType));
                for (Map.Entry<String, Node> input : this.inputs.entrySet()) {
                    if (input.getValue() instanceof TextField) {
                        TextField text = (TextField) input.getValue();
                        if (!Strings.isNullOrEmpty(text.getText())) {
                            step.put(input.getKey(), text.getText());
                        }
                    } else if (input.getValue() instanceof CheckBox) {
                        CheckBox check = (CheckBox) input.getValue();
                        if (check.isSelected()) {
                            step.put(input.getKey(), "true");
                        }
                    }
                }
                for (Map.Entry<String, Map<String, Node>> input : this.locatorInputs.entrySet()) {
                    String type = ((ComboBox<String>) input.getValue().get("type")).getSelectionModel().getSelectedItem();
                    if (!Strings.isNullOrEmpty(type)) {
                        String value = ((TextField) input.getValue().get("value")).getText();
                        step.put(input.getKey(), new Locator(type, value));
                    }
                }
                this.editStep(this.action, this.stepIndex, step.build());
                this.dialog.close();
            });
        });
        return result;
    }

    private void editStep(StepView.Action editAction, int stepIndex, Step newStep) {
        TestCase newCase;
        if (editAction == StepView.Action.EDIT) {
            newCase = this.apprication.getDisplayTestCase().setSteps(stepIndex, newStep);
        } else if (editAction == StepView.Action.INSERT) {
            newCase = this.apprication.getDisplayTestCase().insertStep(stepIndex, newStep);
        } else {
            newCase = this.apprication.getDisplayTestCase().addStep(stepIndex, newStep);
        }
        this.apprication.replaceDisplayCase(newCase);
    }
}
