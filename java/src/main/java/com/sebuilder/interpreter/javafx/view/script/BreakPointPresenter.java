package com.sebuilder.interpreter.javafx.view.script;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.pointcut.LocatorFilter;
import com.sebuilder.interpreter.pointcut.SkipFilter;
import com.sebuilder.interpreter.pointcut.StringParamFilter;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class BreakPointPresenter {

    @Inject
    private SeInterpreterApplication application;
    @FXML
    public ComboBox<String> methodSelect;
    @FXML
    private ComboBox<String> varSelect;
    @FXML
    public TextField targetValue;

    private Stage dialog;

    private int stepIndex;

    public void populate(final Stage dialog, final int stepIndex) {
        this.dialog = dialog;
        this.stepIndex = stepIndex;
        final Step target = this.application.getDisplayTestCase().steps().get(this.stepIndex);
        this.varSelect.getItems().add("");
        target.paramKeys().stream()
                .filter(key -> InputData.isVariable(target.getParam(key)))
                .forEach(key -> this.varSelect.getItems().add(key));
        target.locatorKeys().stream()
                .filter(key -> InputData.isVariable(target.getLocator(key).type()))
                .forEach(key -> this.varSelect.getItems().add(key + ":type"));
        target.locatorKeys().stream()
                .filter(key -> InputData.isVariable(target.getLocator(key).value()))
                .forEach(key -> this.varSelect.getItems().add(key + ":value"));
        this.varSelect.getSelectionModel().select(0);
        this.varSelect.disableProperty().set(this.varSelect.getItems().size() == 1);
        this.methodSelect.getItems().add("always");
        this.methodSelect.getSelectionModel().select(0);
        this.methodSelect.disableProperty().bind(this.varSelect.valueProperty().isEqualTo("")
                .or(this.varSelect.valueProperty().isEqualTo("skip")));
        this.targetValue.disableProperty().bind(this.varSelect.valueProperty().isEqualTo(""));
    }

    @FXML
    public void selectVar() {
        switch (Optional.ofNullable(this.varSelect.getSelectionModel().getSelectedItem()).orElse("")) {
            case "skip" -> {
                this.methodSelect.getItems().clear();
                this.methodSelect.getItems().add("equal");
                this.methodSelect.getSelectionModel().select("equal");
            }
            case "" -> {
                this.methodSelect.getItems().clear();
                this.methodSelect.getItems().add("always");
                this.methodSelect.getSelectionModel().select("always");
                this.targetValue.setText("");
            }
            default -> {
                this.methodSelect.getItems().clear();
                for (final String stepType : this.conditionMethods()) {
                    this.methodSelect.getItems().add(stepType);
                }
            }
        }
    }

    @FXML
    public void stepApply() {
        this.application.addBreakPoint(this.stepIndex, this.pointcut());
        this.dialog.close();
    }

    private String[] conditionMethods() {
        return Pointcut.METHODS.keySet().toArray(new String[0]);
    }

    private Pointcut pointcut() {
        if (Objects.equals(this.methodSelect.getSelectionModel().getSelectedItem(), "always")) {
            return Pointcut.ANY;
        }
        final Step target = this.application.getDisplayTestCase().steps().get(this.stepIndex);
        final String key = this.varSelect.getSelectionModel().getSelectedItem();
        if (key.endsWith(":type")) {
            final Locator l = target.getLocator(key.replace(":type", ""));
            final Locator pointcut = new Locator(this.targetValue.getText(), l.value());
            return new LocatorFilter(this.methodSelect.getSelectionModel().getSelectedItem(), pointcut);
        } else if (key.endsWith(":value")) {
            final Locator l = target.getLocator(key.replace(":value", ""));
            final Locator pointcut = new Locator(l.type(), this.targetValue.getText());
            return new LocatorFilter(this.methodSelect.getSelectionModel().getSelectedItem(), pointcut);
        } else if (key.equals("skip")) {
            return new SkipFilter(Boolean.parseBoolean(this.targetValue.getText()));
        }
        return new StringParamFilter(this.methodSelect.getSelectionModel().getSelectedItem(), key, this.targetValue.getText());
    }

}
