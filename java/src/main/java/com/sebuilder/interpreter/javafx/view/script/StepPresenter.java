package com.sebuilder.interpreter.javafx.view.script;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.tbee.javafx.scene.layout.MigPane;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class StepPresenter {

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
    private final Map<String, Node> inputs = Maps.newHashMap();
    private final Map<String, ComboBox<String>> locatorTypes = Maps.newHashMap();
    private final Map<String, TextField> locatorValues = Maps.newHashMap();
    @Inject
    private SeInterpreterApplication application;
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
    private Stage dialog;

    private String[] stepTypes;

    private BiConsumer<SeInterpreterApplication, Step> applyStep;

    private Predicate<String> textParamFilter = (key) -> true;

    public void populate(final Stage dialog, final Predicate<String> stepTypeFilter, final Predicate<String> textParamFilter, final BiConsumer<SeInterpreterApplication, Step> applyStep) {
        this.dialog = dialog;
        this.textParamFilter = textParamFilter;
        this.stepTypes = Arrays.stream(STEP_TYPES).filter(stepTypeFilter).toArray(String[]::new);
        this.stepTypeSelect.getItems().add("");
        for (final String stepType : this.stepTypes()) {
            this.stepTypeSelect.getItems().add(stepType);
        }
        this.stepTypeSelect.getSelectionModel().select(0);
        this.selectedStepType = this.stepTypeSelect.getSelectionModel().getSelectedItem();
        this.applyStep = applyStep;
    }

    @FXML
    public void selectType() {
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
            if (Optional.ofNullable(this.selectedStepType).orElse("").isBlank()) {
                this.applyStep.accept(this.application, null);
            } else {
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
                this.locatorTypes.forEach((key, value1) -> {
                    final String type = value1.getSelectionModel().getSelectedItem();
                    if (!Strings.isNullOrEmpty(type)) {
                        final String value = this.locatorValues.get(key).getText();
                        step.put(key, new Locator(type, value));
                    }
                });
                this.applyStep.accept(this.application, step.build());
            }
            this.dialog.close();
        });
    }

    public void refreshView(final Step step) {
        final Step stepWithAllParam = step.withAllParam();
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            int row = 1;
            final String typeName = this.resetStepType(stepWithAllParam);
            row = this.addTextBox(stepWithAllParam, row, "skip");
            row = this.addLocator(stepWithAllParam, row, "locator");
            row = this.constructStringParamView(stepWithAllParam, row, typeName);
            this.constructLocatorParamView(stepWithAllParam, row);
            this.stepEditGrid.getScene().getWindow().sizeToScene();
        });
    }

    protected String[] stepTypes() {
        return this.stepTypes;
    }

    protected boolean isDefaultLocator(final String key) {
        return key.equals("locator");
    }

    protected void constructLocatorParamView(final Step step, int row) {
        for (final String key : step.locatorKeys()) {
            if (this.isDefaultLocator(key)) {
                continue;
            }
            row = this.addLocator(step, row, key);
        }
    }

    protected int constructStringParamView(final Step step, int row, final String typeName) {
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

    protected boolean hasGetterType(final String typeName) {
        return typeName.startsWith("wait")
                || typeName.startsWith("assert")
                || typeName.startsWith("verify")
                || typeName.startsWith("if")
                || typeName.startsWith("retry")
                || typeName.startsWith("print")
                || typeName.startsWith("store");
    }

    protected void clearInputFields() {
        this.backupBeforeLocator();
        this.inputs.clear();
        this.locatorTypes.clear();
        this.locatorValues.clear();
        this.stepEditGrid.getChildren().removeIf(node -> !this.stepTypeSelect.equals(node) && !this.labelSelectType.equals(node));
    }

    protected void backupBeforeLocator() {
        if (this.locatorTypeSelect != null && this.stepEditGrid.getChildren().contains(this.locatorTypeSelect)) {
            this.beforeLocatorType = this.locatorTypeSelect.getSelectionModel().getSelectedItem();
            this.beforeLocatorValue = this.locatorText.getText();
        } else {
            this.beforeLocatorType = null;
            this.beforeLocatorValue = null;
        }
    }

    protected String resetStepType(final Step step) {
        final String typeName = step.type().getStepTypeName();
        if (typeName.equals(this.selectedStepType)) {
            return this.selectedStepType;
        }
        this.selectedStepType = typeName;
        this.stepTypeSelect.getSelectionModel().select(typeName);
        return typeName;
    }

    protected int addLocator(final Step step, int row, final String locator) {
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
            this.locatorTypes.put(locator, select);
            this.locatorValues.put(locator, text);
        } else if (this.isDefaultLocator(locator)) {
            this.locatorTypeSelect = null;
            this.locatorText = null;
            this.beforeLocatorType = null;
            this.beforeLocatorValue = null;
        }
        return row;
    }

    protected ComboBox<String> resetLocatorSelect(final Step step, final String locator) {
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

    protected TextField resetLocatorText(final Step step, final String locator) {
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

    protected int addCheckBox(final Step step, final int row, final String key) {
        if (!this.textParamFilter.test(key)) {
            return row;
        }
        final Label label = new Label();
        label.setText(key);
        final CheckBox checkbox = new CheckBox();
        checkbox.setSelected(Boolean.parseBoolean(step.getParam(key)));
        this.stepEditGrid.add(label, "cell 0 " + row);
        this.stepEditGrid.add(checkbox, "cell 1 " + row);
        this.inputs.put(key, checkbox);
        return row + 1;
    }

    protected int addTextBox(final Step step, final int row, final String key) {
        if (!this.textParamFilter.test(key)) {
            return row;
        }
        final Label label = new Label();
        label.setText(key);
        final TextField text = new TextField();
        text.setText(step.getParam(key));
        this.stepEditGrid.add(label, "cell 0 " + row);
        this.stepEditGrid.add(text, "width 150,cell 1 " + row);
        this.inputs.put(key, text);
        return row + 1;
    }

}
