package com.sebuilder.interpreter.javafx.controller;

import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.Result;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.replay.RunStepEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultResetEvent;
import com.sebuilder.interpreter.javafx.event.replay.StepResultSetEvent;
import com.sebuilder.interpreter.javafx.event.script.StepDeleteEvent;
import com.sebuilder.interpreter.javafx.event.script.StepLoadEvent;
import com.sebuilder.interpreter.javafx.event.view.RefreshStepViewEvent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class StepViewController {

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
        this.tableColumnScriptBodyNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        this.tableColumnScriptBodyStep.setCellValueFactory(new PropertyValueFactory<>("step"));
        this.tableViewScriptBody.setRowFactory(new Callback<TableView<ScriptBody>, TableRow<ScriptBody>>() {
            @Override
            public TableRow<ScriptBody> call(TableView<ScriptBody> scriptBodyTableView) {
                return new TableRow<ScriptBody>() {
                    @Override
                    protected void updateItem(ScriptBody item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            for (Result result : Result.values()) {
                                this.getStyleClass().remove(result.name().toLowerCase());
                            }
                            if (item.getRunningResult() != null) {
                                this.getStyleClass().add(item.getRunningResult().toLowerCase());
                            }
                        }
                    }
                };
            }
        });
        EventBus.registSubscriber(this);
    }

    @FXML
    void handleStepDelete(ActionEvent event) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new StepDeleteEvent(item.noProperty().intValue() - 1));
    }

    @FXML
    public void handleStepInsert(ActionEvent actionEvent) throws IOException {
        Stage dialog = initStepEditDialog("insert");
        dialog.setResizable(true);
        dialog.show();
    }

    @FXML
    void handleStepAdd(ActionEvent event) throws IOException {
        Stage dialog = initStepEditDialog("add");
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
    public void handleRunStep(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new RunStepEvent(i -> item.no.intValue() - 1 == i.intValue(), i -> i + item.no.intValue() - 1));
    }

    @FXML
    public void handleRunFromHere(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new RunStepEvent(i -> item.no.intValue() - 1 <= i.intValue(), i -> i + item.no.intValue() - 1));
    }

    @FXML
    public void handleRunToHere(ActionEvent actionEvent) {
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        EventBus.publish(new RunStepEvent(i -> item.no.intValue() - 1 >= i.intValue(), i -> i));
    }


    @Subscribe
    public void refreshTable(RefreshStepViewEvent event) {
        Script script = event.getScript();
        this.refleshTable(script);
    }

    @Subscribe
    public void handleStepResult(StepResultSetEvent event) {
        List<ScriptBody> bodies = this.tableViewScriptBody.getItems();
        if (bodies.size() >= event.getStepNo()) {
            ScriptBody target = bodies.get(event.getStepNo() - 1);
            target.setRunningResult(event.getResult().name());
        }
        this.tableViewScriptBody.refresh();
    }

    @Subscribe
    public void resetRunStepResult(StepResultResetEvent event) {
        for (ScriptBody target : this.tableViewScriptBody.getItems()) {
            target.setRunningResult("");
        }
        this.tableViewScriptBody.refresh();
    }

    private Stage initStepEditDialog(String action) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/seleniumbuilderstepedit.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage dialog = new Stage();
        dialog.setScene(scene);
        dialog.initOwner(tableViewScriptBody.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("edit step");
        StepEditController controller = loader.getController();
        ScriptBody item = this.tableViewScriptBody.getSelectionModel().getSelectedItem();
        controller.init(dialog, item.no.intValue() - 1, action);
        return dialog;
    }

    private void refleshTable(Script aScript) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            int no = 1;
            if (this.tableViewScriptBody.getItems().size() > 0) {
                this.tableViewScriptBody.getItems().clear();
            }
            for (Step step : aScript.steps) {
                ScriptBody row = new ScriptBody(no++, step.toPrettyString(), null);
                this.tableViewScriptBody.getItems().add(row);
            }
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

