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
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.tbee.javafx.scene.layout.MigPane;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

public class StepPresenter {

    @Inject
    private SeInterpreterApplication application;

    private static final String[] STEP_TYPES = {
            "ClickElement"
            , "SetElementText"
            , "SetElementSelected"
            , "SetElementNotSelected"
            , "ClearSelections"
            , "SelectElementValue"
            , "SendKeysToElement"
            , "SendKeysToPathElement"
            , "SubmitElement"
            , "FileDownload"
            , "FileDownloadCDP"
            , "ExecCmd"
            , "Pause"
            , "Loop"
            , "ifElementPresent"
            , "ifElementVisible"
            , "ifElementEnable"
            , "ifElementValue"
            , "ifElementAttribute"
            , "ifElementStyle"
            , "ifElementSelected"
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
            , "retryElementSelected"
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
            , "storeElementSelected"
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
            , "printElementSelected"
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
            , "waitForElementSelected"
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
            , "verifyElementSelected"
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
            , "assertElementSelected"
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
    private MigPane stepEditGrid;

    @FXML
    private SearchableComboBox<String> stepTypeSelect;

    @FXML
    private Label labelSelectType;

    private String selectedStepType;

    private ComboBox<String> locatorTypeSelect;

    private TextField locatorText;

    private String beforeLocatorType;

    private String beforeLocatorValue;

    private final Map<String, Node> inputs = Maps.newHashMap();

    private final Map<String, Map<String, Node>> locatorInputs = Maps.newHashMap();

    private Stage dialog;

    private int stepIndex;

    private StepView.Action action;

    public void populate(final Stage dialog, final int stepIndex, final StepView.Action action) {
        this.dialog = dialog;
        this.stepIndex = stepIndex;
        this.action = action;
    }

    @FXML
    void initialize() {
        assert this.stepTypeSelect != null : "fx:id=\"stepTypeSelect\" was not injected: check your FXML file 'stepedit.fxml'.";
        assert this.stepEditGrid != null : "fx:id=\"stepEditGrid\" was not injected: check your FXML file 'stepedit.fxml'.";
        assert this.labelSelectType != null : "fx:id=\"labelSelectType\" was not injected: check your FXML file 'stepedit.fxml'.";
        this.stepTypeSelect.getItems().add("");
        for (final String stepType : this.stepTypes()) {
            this.stepTypeSelect.getItems().add(stepType);
        }
        this.stepTypeSelect.getSelectionModel().select(0);
        this.selectedStepType = this.stepTypeSelect.getSelectionModel().getSelectedItem();
    }

    @FXML
    void selectType() {
        final String stepType = this.stepTypeSelect.getSelectionModel().getSelectedItem();
        if (Objects.equals(this.selectedStepType, stepType) || Strings.isNullOrEmpty(stepType)) {
            return;
        }
        this.selectedStepType = this.stepTypeSelect.getSelectionModel().getSelectedItem();
        this.clearInputFields();
        this.refreshView(this.application.createStep(stepType));
    }

    @FXML
    public void stepApply() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            final StepBuilder step = new StepBuilder(this.application.getStepTypeOfName(this.selectedStepType));
            this.inputs.forEach((key, value) -> {
                if (value instanceof TextField text) {
                    if (!Strings.isNullOrEmpty(text.getText())) {
                        step.put(key, text.getText());
                    }
                } else if (value instanceof CheckBox check) {
                    if (check.isSelected()) {
                        step.put(key, "true");
                    }
                }
            });
            this.locatorInputs.forEach((key, value1) -> {
                final String type = ((ComboBox<String>) value1.get("type")).getSelectionModel().getSelectedItem();
                if (!Strings.isNullOrEmpty(type)) {
                    final String value = ((TextField) value1.get("value")).getText();
                    step.put(key, new Locator(type, value));
                }
            });
            this.editStep(this.action, this.stepIndex, step.build());
            this.dialog.close();
        });
    }

    void refreshView(final Step step) {
        final Step stepWithAllParam = step.withAllParam();
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            int row = 1;
            final String typeName = this.resetStepType(stepWithAllParam);
            row = this.addTextBox(stepWithAllParam, row, "skip");
            row = this.addLocator(stepWithAllParam, row, "locator");
            row = this.constructStringParamView(stepWithAllParam, row, typeName);
            row = this.constructLocatorParamView(stepWithAllParam, row);
            this.stepEditGrid.getScene().getWindow().sizeToScene();
        });
    }

    private String[] stepTypes() {
        return STEP_TYPES;
    }

    private boolean isDefaultLocator(final String key) {
        return key.equals("locator");
    }

    private int constructLocatorParamView(final Step step, int row) {
        for (final String key : step.locatorKeys()) {
            if (this.isDefaultLocator(key)) {
                continue;
            }
            row = this.addLocator(step, row, key);
        }
        return row;
    }

    private int constructStringParamView(final Step step, int row, final String typeName) {
        for (final String key : step.paramKeys()) {
            if (key.equals("type") || key.equals("skip")
                    || (key.equals("negated") && !this.hasGetterType(typeName))) {
                continue;
            }
            if (key.equals("negated")) {
                row = this.addCheckBox(step, row, key);
            } else {
                row = this.addTextBox(step, row, key);
            }
        }
        return row;
    }

    private boolean hasGetterType(final String typeName) {
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

    private String resetStepType(final Step step) {
        final String typeName = step.type().getStepTypeName();
        if (typeName.equals(this.selectedStepType)) {
            return this.selectedStepType;
        }
        this.selectedStepType = typeName;
        this.stepTypeSelect.getSelectionModel().select(typeName);
        return typeName;
    }

    private int addLocator(final Step step, int row, final String locator) {
        if (step.locatorContains(locator)) {
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
            final Map<String, Node> input = Maps.newHashMap();
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

    private ComboBox<String> resetLocatorSelect(final Step step, final String locator) {
        final ComboBox<String> select = new ComboBox<>();
        select.getItems().add("");
        select.getItems().add("id");
        select.getItems().add("name");
        select.getItems().add("css selector");
        select.getItems().add("xpath");
        select.getItems().add("link text");
        final Locator current = step.getLocator(locator);
        String type = "";
        if (!Strings.isNullOrEmpty(current.value())) {
            type = current.type();
        }
        if (this.isDefaultLocator(locator)) {
            this.locatorTypeSelect = select;
            if (!type.equals("")) {
                this.beforeLocatorType = type;
            }
            this.locatorTypeSelect.getSelectionModel().select(Objects.requireNonNullElse(this.beforeLocatorType, ""));
        } else {
            select.getSelectionModel().select(type);
        }
        return select;
    }

    private TextField resetLocatorText(final Step step, final String locator) {
        final TextField text = new TextField();
        final String value = step.getLocator(locator).value();
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

    private int addCheckBox(final Step step, int row, final String key) {
        final Label label = new Label();
        label.setText(key);
        final CheckBox checkbox = new CheckBox();
        checkbox.setSelected(Boolean.parseBoolean(step.getParam(key)));
        this.stepEditGrid.add(label, "cell 0 " + row);
        this.stepEditGrid.add(checkbox, "cell 1 " + row++);
        this.inputs.put(key, checkbox);
        return row;
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

    private void editStep(final StepView.Action editAction, final int stepIndex, final Step newStep) {
        final TestCase newCase;
        if (editAction == StepView.Action.EDIT) {
            newCase = this.application.getDisplayTestCase().replaceSteps(stepIndex, newStep);
        } else if (editAction == StepView.Action.INSERT) {
            newCase = this.application.getDisplayTestCase().insertStep(stepIndex, newStep);
        } else {
            newCase = this.application.getDisplayTestCase().addStep(stepIndex, newStep);
        }
        this.application.replaceDisplayCase(newCase);
    }
}
