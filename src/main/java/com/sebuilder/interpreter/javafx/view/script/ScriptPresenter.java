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
        assert tableColumnScriptBodyStep != null : "fx:id=\"tableColumnScriptBodyStep\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        assert tableColumnScriptBodyNo != null : "fx:id=\"tableColumnScriptBodyNo\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        assert tableViewScriptBody != null : "fx:id=\"tableViewScriptBody\" was not injected: check your FXML file 'seleniumbuilderscriptbody.fxml'.";
        this.tableColumnScriptBodyNo.setCellValueFactory(body -> body.getValue().noProperty().asObject());
        this.tableColumnScriptBodyStep.setCellValueFactory(body -> body.getValue().stepProperty());
        this.tableViewScriptBody.setRowFactory(new DragAndDropTableViewRowFactory<>() {
            @Override
            protected void updateItemCallback(TableRow<ScriptBody> tableRow, ScriptBody scriptBody, boolean b) {
                for (Result result : Result.values()) {
                    tableRow.getStyleClass().remove(result.toString().toLowerCase());
                }
                if (!b && !tableRow.isEmpty()) {
                    tableRow.getItem().runningResultProperty().addListener((ObservableValue<? extends String> observed, String oldValue, String newValue) -> {
                        for (Result result : Result.values()) {
                            tableRow.getStyleClass().remove(result.toString().toLowerCase());
                        }
                        if (!Strings.isNullOrEmpty(newValue)) {
                            tableRow.getStyleClass().add(newValue.toLowerCase());
                        }
                    });
                }
            }
            @Override
            protected void move(int draggedIndex, int dropIndex) {
                ScriptPresenter.this.moveStep(draggedIndex, dropIndex);
            }
        });
        this.application.displayTestCaseProperty().addListener((observed, oldValue, newValue) -> {
            if (application.scriptViewTypeProperty().get() == ViewType.TABLE) {
                refreshTable();
            }
        });
        this.application.scriptViewTypeProperty().addListener((ObservableValue<? extends ViewType> observed, ViewType oldValue, ViewType newValue) -> {
            if (newValue == ViewType.TABLE) {
                refreshTable();
            }
        });
        this.application.replayStatusProperty().addListener((observed, oldVar, newVar) -> {
            if (newVar != null) {
                handleStepResult(newVar.getKey(), newVar.getValue());
            }
        });
        this.refreshTable();
    }

    @FXML
    void handleStepDelete() {
        int stepNo = this.tableViewScriptBody.getSelectionModel().getSelectedItem().noProperty().intValue();
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
        StepView stepView = this.initStepEditDialog(StepView.Action.EDIT);
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        stepView.refresh(this.application.getDisplayTestCase()
                .steps()
                .get(item.no.intValue() - 1)
        );
    }

    @FXML
    void handleRunStep() {
        InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) -> {
            ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
            this.application.runStep(it, i -> item.no.intValue() - 1 == i.intValue(), i -> i + item.no.intValue() - 1);
        });
        inputView.open(this.tableViewScriptBody.getScene().getWindow());
    }

    @FXML
    void handleRunFromHere() {
        InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) -> {
            ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
            this.application.runStep(it, i -> item.no.intValue() - 1 <= i.intValue(), i -> i + item.no.intValue() - 1);
        });
        inputView.open(this.tableViewScriptBody.getScene().getWindow());
    }

    @FXML
    void handleRunToHere() {
        InputView inputView = new InputView();
        inputView.setOnclickReplayStart((it) -> {
            ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
            this.application.runStep(it, i -> item.no.intValue() - 1 >= i.intValue(), i -> i);
        });
        inputView.open(this.tableViewScriptBody.getScene().getWindow());
    }

    private void moveStep(int from, int to) {
        Step step = this.application.getDisplayTestCase().steps().get(from);
        TestCase newCase;
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
            for (Step step : this.application.getDisplayTestCase().steps()) {
                ScriptBody row = new ScriptBody(no++, step.toPrettyString(), "");
                this.tableViewScriptBody.getItems().add(row);
            }
            this.tableViewScriptBody.refresh();
        });
    }

    private void handleStepResult(int stepNo, Result result) {
        List<ScriptBody> bodies = this.tableViewScriptBody.getItems();
        if (bodies.size() >= stepNo) {
            ScriptBody target = bodies.get(stepNo - 1);
            target.setRunningResult(result.name());
        }
    }

    private StepView initStepEditDialog(StepView.Action action) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        int no = 0;
        if (item != null) {
            no = item.no.intValue() - 1;
        }
        return new StepView(tableViewScriptBody.getScene().getWindow(), no, action);
    }

    private static class ScriptBody {
        private final IntegerProperty no;

        private final StringProperty step;

        private final StringProperty runningResult;

        public ScriptBody(int no, String step, String runningResult) {
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

        public void setRunningResult(String runningResult) {
            this.runningResult.set(runningResult);
        }

    }

}
