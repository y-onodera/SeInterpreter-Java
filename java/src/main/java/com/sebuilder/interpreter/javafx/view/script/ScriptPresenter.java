package com.sebuilder.interpreter.javafx.view.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.application.Result;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.application.ViewType;
import com.sebuilder.interpreter.javafx.control.DragAndDropTableViewRowFactory;
import com.sebuilder.interpreter.javafx.view.replay.InputView;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ScriptPresenter {

    @Inject
    private SeInterpreterApplication application;

    @FXML
    private TableColumn<ScriptBody, String> tableColumnScriptBodyStep;

    @FXML
    private TableColumn<ScriptBody, Integer> tableColumnScriptBodyNo;

    @FXML
    private TableView<ScriptBody> tableViewScriptBody;

    @FXML
    void initialize() {
        assert this.tableColumnScriptBodyStep != null : "fx:id=\"tableColumnScriptBodyStep\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        assert this.tableColumnScriptBodyNo != null : "fx:id=\"tableColumnScriptBodyNo\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        assert this.tableViewScriptBody != null : "fx:id=\"tableViewScriptBody\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        this.tableColumnScriptBodyNo.setCellValueFactory(body -> body.getValue().noProperty().asObject());
        this.tableColumnScriptBodyStep.setCellValueFactory(body -> body.getValue().stepProperty());
        this.tableViewScriptBody.setRowFactory(new DragAndDropTableViewRowFactory<>() {
            @Override
            protected void updateItemCallback(final TableRow<ScriptBody> tableRow, final ScriptBody scriptBody, final boolean empty, final int notEmptyValCount) {
                for (final Result result : Result.values()) {
                    tableRow.getStyleClass().remove(result.toString().toLowerCase());
                }
                if (!empty && !tableRow.isEmpty()) {
                    if (notEmptyValCount == 1) {
                        tableRow.getItem().runningResultProperty().addListener((ObservableValue<? extends String> observed, String oldValue, String newValue) -> {
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
        this.application.scriptViewTypeProperty().addListener((ObservableValue<? extends ViewType> observed, ViewType oldValue, ViewType newValue) -> {
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
        final int stepNo = this.tableViewScriptBody.getSelectionModel().getSelectedItem().noProperty().intValue();
        this.application.replaceDisplayCase(this.application.getDisplayTestCase().removeStep(stepNo - 1));
    }

    @FXML
    void handleStepInsert() {
        this.initStepEditDialog(StepView.Action.INSERT);
    }

    @FXML
    void handleStepAdd() {
        this.initStepEditDialog(StepView.Action.ADD);
    }

    @FXML
    void handleStepEdit() {
        final StepView stepView = this.initStepEditDialog(StepView.Action.EDIT);
        final ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        stepView.refresh(this.application.getDisplayTestCase()
                .steps()
                .get(item.index())
        );
    }

    @FXML
    void handleRunStep() {
        final InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) -> {
            final ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
            this.application.runStep(it, i -> item.compareIndex(i.intValue()) == 0, i -> i + item.index(), false);
        });
        inputView.open(this.tableViewScriptBody.getScene().getWindow());
    }

    @FXML
    void handleRunFromHere() {
        final InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) -> {
            final ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
            this.application.runStep(it, i -> item.compareIndex(i.intValue()) <= 0, i -> i + item.index(), true);
        });
        inputView.open(this.tableViewScriptBody.getScene().getWindow());
    }

    @FXML
    void handleRunToHere() {
        final InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) -> {
            final ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
            this.application.runStep(it, i -> item.compareIndex(i.intValue()) >= 0, i -> i, false);
        });
        inputView.open(this.tableViewScriptBody.getScene().getWindow());
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
            for (final Step step : this.application.getDisplayTestCase().steps()) {
                final ScriptBody row = new ScriptBody(no++, step.toPrettyString(), "");
                this.tableViewScriptBody.getItems().add(row);
            }
            this.tableViewScriptBody.refresh();
        });
    }

    private void handleStepResult(final int stepIndex, final Result result) {
        final List<ScriptBody> bodies = this.tableViewScriptBody.getItems();
        bodies.stream()
                .filter(item -> item.index() == stepIndex)
                .findFirst()
                .ifPresent(target -> target.setRunningResult(result.name()));
    }

    private StepView initStepEditDialog(final StepView.Action action) {
        final ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        int no = 0;
        if (item != null) {
            no = item.no.intValue() - 1;
        }
        return new StepView(this.tableViewScriptBody.getScene().getWindow(), no, action);
    }

    private static class ScriptBody {

        private final IntegerProperty no;

        private final StringProperty step;

        private final StringProperty runningResult;

        public ScriptBody(final int no, final String step, final String runningResult) {
            this.no = new SimpleIntegerProperty(no);
            this.step = new SimpleStringProperty(step);
            if (runningResult != null) {
                this.runningResult = new SimpleStringProperty(runningResult);
            } else {
                this.runningResult = new SimpleStringProperty();
            }
        }

        public IntegerProperty noProperty() {
            return this.no;
        }

        public StringProperty stepProperty() {
            return this.step;
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
            return this.no.intValue() - 1;
        }

    }

}
