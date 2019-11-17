package com.sebuilder.interpreter.javafx.controller;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.Result;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.replay.RunStepEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultResetEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultSetEvent;
import com.sebuilder.interpreter.javafx.event.script.StepDeleteEvent;
import com.sebuilder.interpreter.javafx.event.script.StepLoadEvent;
import com.sebuilder.interpreter.javafx.event.script.StepMoveEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepTableViewEvent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StepViewController {

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

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
                    EventBus.publish(new StepMoveEvent(draggedIndex, dropIndex));
                }
            });
            return row;
        });
        EventBus.registSubscriber(this);
    }

    @FXML
    void handleStepDelete(ActionEvent event) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new StepDeleteEvent(item.noProperty().intValue() - 1));
    }

    @FXML
    void handleStepInsert(ActionEvent actionEvent) throws IOException {
        Stage dialog = initStepEditDialog("insert");
        dialog.setResizable(true);
        dialog.show();
    }

    @FXML
    void handleStepAdd(ActionEvent event) throws IOException {
        Stage dialog = initStepEditDialog("appendNewChain");
        dialog.setResizable(true);
        dialog.show();
    }

    @FXML
    void handleStepEdit(ActionEvent event) throws IOException {
        Stage dialog = initStepEditDialog("change");
        dialog.setResizable(true);
        dialog.show();
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new StepLoadEvent(item.no.intValue() - 1));
    }

    @FXML
    void handleRunStep(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new RunStepEvent(i -> item.no.intValue() - 1 == i.intValue(), i -> i + item.no.intValue() - 1));
    }

    @FXML
    void handleRunFromHere(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new RunStepEvent(i -> item.no.intValue() - 1 <= i.intValue(), i -> i + item.no.intValue() - 1));
    }

    @FXML
    void handleRunToHere(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new RunStepEvent(i -> item.no.intValue() - 1 >= i.intValue(), i -> i));
    }

    @Subscribe
    public void refreshTable(RefreshStepTableViewEvent event) {
        TestCase testCase = event.getTestCase();
        this.refreshTable(testCase);
    }

    @Subscribe
    public void handleStepResult(StepResultSetEvent event) {
        List<ScriptBody> bodies = this.tableViewScriptBody.getItems();
        if (bodies.size() >= event.getStepNo()) {
            ScriptBody target = bodies.get(event.getStepNo() - 1);
            target.setRunningResult(event.getResult().name());
        }
    }

    @Subscribe
    public void resetRunStepResult(StepResultResetEvent event) {
        for (ScriptBody target : this.tableViewScriptBody.getItems()) {
            target.setRunningResult("");
        }
    }

    private Stage initStepEditDialog(String action) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(this.getClass().getResource("/fxml/stepedit.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage dialog = new Stage();
        dialog.setScene(scene);
        dialog.initOwner(tableViewScriptBody.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("edit step");
        StepEditController controller = loader.getController();
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        int no = 0;
        if (item != null) {
            no = item.no.intValue() - 1;
        }
        controller.init(dialog, no, action);
        return dialog;
    }

    private void refreshTable(TestCase aTestCase) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            this.tableViewScriptBody.getItems().setAll(new ArrayList<>());
            int no = 1;
            for (Step step : aTestCase.steps()) {
                ScriptBody row = new ScriptBody(no++, step.toPrettyString(), "");
                this.tableViewScriptBody.getItems().add(row);
            }
            this.tableViewScriptBody.refresh();
        });
    }

    public static class ScriptBody {
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

