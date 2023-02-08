package com.sebuilder.interpreter.javafx.view.script;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.javafx.application.BreakPoint;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.pointcut.LocatorFilter;
import com.sebuilder.interpreter.pointcut.NegatedFilter;
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
                .or(this.varSelect.valueProperty().isEqualTo("skip"))
                .or(this.varSelect.valueProperty().isEqualTo("negated")));
        this.targetValue.disableProperty().bind(this.varSelect.valueProperty().isEqualTo(""));
        final Optional<BreakPoint> breakPoint = BreakPoint.findFrom(this.application.getDisplayTestCase().aspect());
        breakPoint.ifPresent(current -> {
            if (current.condition().containsKey(stepIndex)) {
                final Pointcut condition = current.condition().get(stepIndex);
                if (condition instanceof SkipFilter skip) {
                    this.varSelect.getSelectionModel().select(skip.key());
                    this.methodSelect.getSelectionModel().select("equals");
                    this.targetValue.setText(Boolean.toString(skip.target()));
                } else if (condition instanceof NegatedFilter negated) {
                    this.varSelect.getSelectionModel().select(negated.key());
                    this.methodSelect.getSelectionModel().select("equals");
                    this.targetValue.setText(Boolean.toString(negated.target()));
                } else if (condition instanceof LocatorFilter locator) {
                    if (this.varSelect.getItems().filtered(it -> it.endsWith(":type")).size() > 0) {
                        this.varSelect.getSelectionModel().select(locator.key() + ":type");
                        this.methodSelect.getSelectionModel().select(locator.method());
                        this.targetValue.setText(locator.target().type());
                    } else {
                        this.varSelect.getSelectionModel().select(locator.key() + ":value");
                        this.methodSelect.getSelectionModel().select(locator.method());
                        this.targetValue.setText(locator.target().value());
                    }
                } else if (condition instanceof StringParamFilter string) {
                    this.varSelect.getSelectionModel().select(string.key());
                    this.methodSelect.getSelectionModel().select(string.method());
                    this.targetValue.setText(string.target());
                }
            }
        });
    }

    @FXML
    public void selectVar() {
        switch (Optional.ofNullable(this.varSelect.getSelectionModel().getSelectedItem()).orElse("")) {
            case "skip", "negated" -> {
                this.methodSelect.getItems().clear();
                this.methodSelect.getItems().add("equals");
                this.methodSelect.getSelectionModel().select("equals");
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
            return new LocatorFilter(key.replace(":type", ""), pointcut, this.methodSelect.getSelectionModel().getSelectedItem());
        } else if (key.endsWith(":value")) {
            final Locator l = target.getLocator(key.replace(":value", ""));
            final Locator pointcut = new Locator(l.type(), this.targetValue.getText());
            return new LocatorFilter(key.replace(":value", ""), pointcut, this.methodSelect.getSelectionModel().getSelectedItem());
        } else if (key.equals("skip")) {
            return new SkipFilter(Boolean.parseBoolean(this.targetValue.getText()));
        } else if (key.equals("negated")) {
            return new NegatedFilter(Boolean.parseBoolean(this.targetValue.getText()));
        }
        return new StringParamFilter(key, this.targetValue.getText(), this.methodSelect.getSelectionModel().getSelectedItem());
    }

}
