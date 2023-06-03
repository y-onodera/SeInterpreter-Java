package com.sebuilder.interpreter.javafx.view.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.application.BreakPoint;
import com.sebuilder.interpreter.javafx.application.Result;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.application.ViewType;
import com.sebuilder.interpreter.javafx.control.DragAndDropTableViewRowFactory;
import com.sebuilder.interpreter.javafx.view.replay.InputView;
import com.sebuilder.interpreter.pointcut.VerifyFilter;
import com.sebuilder.interpreter.step.Verify;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Window;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScriptPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private TableColumn<StepDefine, StepNo> tableColumnScriptBodyNo;

    @FXML
    private TableColumn<StepDefine, String> tableColumnScriptBodyStep;

    @FXML
    private TableView<StepDefine> tableViewScriptBody;

    @FXML
    void initialize() {
        assert this.tableColumnScriptBodyStep != null : "fx:id=\"tableColumnScriptBodyStep\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        assert this.tableColumnScriptBodyNo != null : "fx:id=\"tableColumnScriptBodyNo\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        assert this.tableViewScriptBody != null : "fx:id=\"tableViewScriptBody\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        this.tableColumnScriptBodyNo.setCellValueFactory(body -> body.getValue().noProperty());
        this.tableColumnScriptBodyNo.setCellFactory(cell -> new StepNoCell());
        this.tableColumnScriptBodyStep.setCellValueFactory(body -> body.getValue().scriptProperty());
        this.tableViewScriptBody.setRowFactory(new DragAndDropTableViewRowFactory<>() {
            @Override
            protected void updateItemCallback(final TableRow<StepDefine> tableRow, final StepDefine stepDefine, final boolean empty, final int notEmptyValCount) {
                for (final Result result : Result.values()) {
                    tableRow.getStyleClass().remove(result.toString().toLowerCase());
                }
                if (!empty && !tableRow.isEmpty()) {
                    if (notEmptyValCount == 1) {
                        tableRow.getItem().runningResultProperty().addListener((final ObservableValue<? extends String> observed, final String oldValue, final String newValue) -> {
                            for (final Result result : Result.values()) {
                                tableRow.getStyleClass().remove(result.toString().toLowerCase());
                            }
                            if (!Strings.isNullOrEmpty(newValue)) {
                                tableRow.getStyleClass().add(newValue.toLowerCase());
                            }
                        });
                        tableRow.setOnMouseClicked(ev -> {
                            if (ev.getButton().equals(MouseButton.PRIMARY) && ev.getClickCount() == 2) {
                                ScriptPresenter.this.handleStepEdit();
                            }
                        });
                    }
                    final String currentStyle = tableRow.getItem().runningResultProperty().get();
                    if (!Strings.isNullOrEmpty(currentStyle)) {
                        tableRow.getStyleClass().add(currentStyle.toLowerCase());
                    }
                }
            }

            @Override
            protected void move(final int draggedIndex, final int dropIndex) {
                ScriptPresenter.this.moveStep(draggedIndex, dropIndex);
            }
        });
        this.application.displayTestCaseProperty().addListener((observed, oldValue, newValue) -> {
            if (this.application.scriptViewTypeProperty().get() == ViewType.TABLE) {
                this.refreshTable();
            }
        });
        this.application.scriptViewTypeProperty().addListener((final ObservableValue<? extends ViewType> observed, final ViewType oldValue, final ViewType newValue) -> {
            if (newValue == ViewType.TABLE) {
                this.refreshTable();
            }
        });
        this.application.replayStatusProperty().addListener((observed, oldVar, newVar) -> {
            if (newVar != null) {
                this.handleStepResult(newVar.getKey(), newVar.getValue());
            }
        });
        this.refreshTable();
    }

    @FXML
    void handleStepDelete() {
        final int stepIndex = this.tableViewScriptBody.getSelectionModel().getSelectedItem().index();
        this.application.replaceDisplayCase(this.application.getDisplayTestCase().removeStep(stepIndex));
    }

    @FXML
    void handleStepInsert() {
        this.initStepEditDialog(Action.INSERT);
    }

    @FXML
    void handleStepAdd() {
        this.initStepEditDialog(Action.ADD);
    }

    @FXML
    void handleStepEdit() {
        final StepView stepView = this.initStepEditDialog(Action.EDIT);
        final StepDefine item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        stepView.refresh(this.application.getDisplayTestCase()
                .steps()
                .get(item.index())
        );
    }

    @FXML
    void handleRunStep() {
        final StepDefine item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        final InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) ->
                this.application.runStep(it, (testRun, step, var) -> item.compareIndex(var.stepIndex()) == 0, false)
        );
        inputView.open(this.currentWindow());
    }

    @FXML
    void handleRunFromHere() {
        final StepDefine item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        final InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) ->
                this.application.runStep(it, (testRun, step, var) -> item.compareIndex(var.stepIndex()) <= 0, true)
        );
        inputView.open(this.currentWindow());
    }

    @FXML
    void handleRunToHere() {
        final StepDefine item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        final InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) ->
                this.application.runStep(it, (testRun, step, var) -> item.compareIndex(var.stepIndex()) >= 0, false)
        );
        inputView.open(this.currentWindow());
    }

    @FXML
    void handleAddBreakPoint() {
        final StepDefine item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        new StepView(s -> s.startsWith("verify"), key -> !key.equals("skip"))
                .open(this.currentWindow(), (application, step) -> {
                    if (step != null) {
                        final VerifyFilter pointcut = new VerifyFilter((Verify) step.type(), step.stringParams(), step.locatorParams());
                        application.addBreakPoint(item.index(), pointcut);
                    } else {
                        application.addBreakPoint(item.index(), Pointcut.ANY);
                    }
                });
    }

    @FXML
    void handleRemoveBreakPoint() {
        final StepDefine item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        this.application.removeBreakPoint(item.index());
    }

    private void moveStep(final int from, final int to) {
        final Step step = this.application.getDisplayTestCase().steps().get(from);
        final TestCase newCase;
        if (to > from) {
            newCase = this.application.getDisplayTestCase().addStep(to, step)
                    .removeStep(from);
        } else {
            newCase = this.application.getDisplayTestCase().insertStep(to, step)
                    .removeStep(from + 1);
        }
        this.application.replaceDisplayCase(newCase);
    }

    private void refreshTable() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.tableViewScriptBody.getItems().setAll(new ArrayList<>());
            int no = 1;
            final List<Integer> hasBreakPoint = new ArrayList<>();
            final Optional<BreakPoint> breakPoint = BreakPoint.findFrom(this.application.getDisplayTestCase().aspect());
            breakPoint.ifPresent(it -> hasBreakPoint.addAll(it.targetStepIndex()));
            for (final Step step : this.application.getDisplayTestCase().steps()) {
                final StepDefine row;
                if (hasBreakPoint.contains(no - 1)) {
                    row = new StepDefine(new StepNo(no++).withBreakPoint(), step.toPrettyString(), "");
                } else {
                    row = new StepDefine(new StepNo(no++), step.toPrettyString(), "");
                }
                this.tableViewScriptBody.getItems().add(row);
            }
            this.tableViewScriptBody.refresh();
        });
    }

    private void handleStepResult(final int stepIndex, final Result result) {
        final List<StepDefine> bodies = this.tableViewScriptBody.getItems();
        bodies.stream()
                .filter(item -> item.index() == stepIndex)
                .findFirst()
                .ifPresent(target -> target.setRunningResult(result.name()));
    }

    private StepView initStepEditDialog(final Action action) {
        final StepDefine item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        final int no = item != null ? item.index() : 0;
        final StepView stepView = new StepView();
        stepView.open(this.currentWindow(), (application, step) -> {
            if (step != null) {
                final TestCase newCase;
                if (action == Action.EDIT) {
                    newCase = application.getDisplayTestCase().replaceSteps(no, step);
                } else if (action == Action.INSERT) {
                    newCase = application.getDisplayTestCase().insertStep(no, step);
                } else {
                    newCase = application.getDisplayTestCase().addStep(no, step);
                }
                application.replaceDisplayCase(newCase);
            }
        });
        return stepView;
    }

    private Window currentWindow() {
        return this.tableViewScriptBody.getScene().getWindow();
    }

    static class StepDefine {

        private final ObjectProperty<StepNo> no;

        private final StringProperty script;

        private final StringProperty runningResult;

        public StepDefine(final StepNo stepNo, final String step, final String runningResult) {
            this.no = new SimpleObjectProperty<>(stepNo);
            this.script = new SimpleStringProperty(step);
            if (runningResult != null) {
                this.runningResult = new SimpleStringProperty(runningResult);
            } else {
                this.runningResult = new SimpleStringProperty();
            }
        }

        public ObjectProperty<StepNo> noProperty() {
            return this.no;
        }

        public StringProperty scriptProperty() {
            return this.script;
        }

        public StringProperty runningResultProperty() {
            return this.runningResult;
        }

        public void setRunningResult(final String runningResult) {
            this.runningResult.set(runningResult);
        }

        public int compareIndex(final int index) {
            return Integer.compare(this.index(), index);
        }

        public int index() {
            return this.no.get().no - 1;
        }

    }

    private record StepNo(Integer no, Circle breakPoint) {
        StepNo(final Integer no) {
            this(no, null);
        }

        StepNo withBreakPoint() {
            return new StepNo(this.no, new Circle(3, Color.RED));
        }
    }

    private static class StepNoCell extends TableCell<StepDefine, StepNo> {
        @Override
        protected void updateItem(final StepNo item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                this.setText(null);
                this.setGraphic(null);
            } else {
                this.setText(item.no.toString());
                this.setGraphic(item.breakPoint);
            }
        }
    }

    private enum Action {
        INSERT, ADD, EDIT
    }
}
