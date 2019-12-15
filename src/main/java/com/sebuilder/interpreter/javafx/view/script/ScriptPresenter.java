package com.sebuilder.interpreter.javafx.view.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.application.Result;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.application.ViewType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ScriptPresenter {

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

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
        this.tableViewScriptBody.setRowFactory(scriptBodyTableView -> {
            TableRow<ScriptBody> row = new TableRow<ScriptBody>() {
                @Override
                protected void updateItem(ScriptBody scriptBody, boolean b) {
                    super.updateItem(scriptBody, b);
                    for (Result result : Result.values()) {
                        getStyleClass().remove(result.toString().toLowerCase());
                    }
                    if (!b && !isEmpty()) {
                        getItem().runningResultProperty().addListener((ObservableValue<? extends String> observed, String oldValue, String newValue) -> {
                            for (Result result : Result.values()) {
                                getStyleClass().remove(result.toString().toLowerCase());
                            }
                            if (!Strings.isNullOrEmpty(newValue)) {
                                getStyleClass().add(newValue.toLowerCase());
                            }
                        });
                    }
                }
            };
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    int dropIndex;
                    if (row.isEmpty()) {
                        dropIndex = tableViewScriptBody.getItems().size() - 1;
                    } else {
                        dropIndex = row.getIndex();
                    }
                    event.setDropCompleted(true);
                    event.consume();
                    moveStep(draggedIndex, dropIndex);
                }
            });
            return row;
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
    void handleStepDelete(ActionEvent event) {
        int stepNo = this.tableViewScriptBody.getSelectionModel().getSelectedItem().noProperty().intValue();
        this.application.replaceDisplayCase(this.application.getDisplayTestCase().removeStep(stepNo - 1));
    }

    @FXML
    void handleStepInsert(ActionEvent actionEvent) {
        this.initStepEditDialog(StepView.Action.INSERT);
    }

    @FXML
    void handleStepAdd(ActionEvent event) {
        this.initStepEditDialog(StepView.Action.ADD);
    }

    @FXML
    void handleStepEdit(ActionEvent event) {
        StepView stepView = this.initStepEditDialog(StepView.Action.EDIT);
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        stepView.refresh(this.application.getDisplayTestCase()
                        .steps()
                        .get(item.no.intValue() - 1)
                );
    }

    @FXML
    void handleRunStep(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        this.application.runStep(i -> item.no.intValue() - 1 == i.intValue(), i -> i + item.no.intValue() - 1);
    }

    @FXML
    void handleRunFromHere(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        this.application.runStep(i -> item.no.intValue() - 1 <= i.intValue(), i -> i + item.no.intValue() - 1);
    }

    @FXML
    void handleRunToHere(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        this.application.runStep(i -> item.no.intValue() - 1 >= i.intValue(), i -> i);
    }

    private void moveStep(int from, int to) {
        Step step = this.application.getDisplayTestCase().steps().get(from);
        int indexTo = to;
        TestCase newCase;
        if (to > from) {
            newCase = this.application.getDisplayTestCase().addStep(indexTo, step)
                    .removeStep(from);
        } else {
            newCase = this.application.getDisplayTestCase().insertStep(indexTo, step)
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

        private StringProperty runningResult;

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

        public String getRunningResult() {
            return this.runningResult.get();
        }

        public void setRunningResult(String runningResult) {
            this.runningResult.set(runningResult);
        }

    }

}
