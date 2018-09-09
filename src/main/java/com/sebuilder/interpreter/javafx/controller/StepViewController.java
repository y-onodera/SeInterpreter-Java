package com.sebuilder.interpreter.javafx.controller;

import com.google.common.eventbus.Subscribe;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.Result;
import com.sebuilder.interpreter.javafx.event.ReportErrorEvent;
import com.sebuilder.interpreter.javafx.event.script.HandleStepResultEvent;
import com.sebuilder.interpreter.javafx.event.script.RefreshStepViewEvent;
import com.sebuilder.interpreter.javafx.event.script.ResetStepResutEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptReloadEvent;
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
                                getStyleClass().remove(result.name().toLowerCase());
                            }
                            if (item.getRunningResult() != null) {
                                getStyleClass().add(item.getRunningResult().toLowerCase());
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
        ScriptBody item = tableViewScriptBody.getSelectionModel().getSelectedItem();
        tableViewScriptBody.getItems().remove(item);
    }

    @FXML
    void handleStepEdit(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("fxml/seleniumbuilderstepedit.fxml")));
        Scene scene = new Scene(root);
        Stage dialog = new Stage();
        dialog.setScene(scene);
        dialog.initOwner(tableViewScriptBody.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(false);
        dialog.setTitle("edit step");
        dialog.show();
    }

    @Subscribe
    public void refreshTable(RefreshStepViewEvent event) {
        ReportErrorEvent.publishIfExecuteThrowsException(() -> {
            int no = 1;
            this.tableViewScriptBody.getItems().clear();
            Script script = event.getScript();
            for (Step step : script.steps) {
                ScriptBody row = new ScriptBody(no++, step.toPrettyString(), null);
                this.tableViewScriptBody.getItems().add(row);
            }
            EventBus.publish(new ScriptReloadEvent(script));
        });
    }

    @Subscribe
    public void handleRunStepResult(HandleStepResultEvent event) {
        List<ScriptBody> bodies = this.tableViewScriptBody.getItems();
        if (bodies.size() >= event.getStepNo()) {
            ScriptBody target = bodies.get(event.getStepNo() - 1);
            target.setRunningResult(event.getResult().name());
        }
        this.tableViewScriptBody.refresh();
    }

    @Subscribe
    public void resetRunStepResult(ResetStepResutEvent event) {
        for (ScriptBody target : this.tableViewScriptBody.getItems()) {
            target.setRunningResult("");
        }
        this.tableViewScriptBody.refresh();
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
            return no;
        }

        public StringProperty stepProperty() {
            return step;
        }

        public StringProperty runningResultProperty() {
            return runningResult;
        }

        public String getRunningResult() {
            return runningResult.get();
        }

        public void setRunningResult(String runningResult) {
            this.runningResult.set(runningResult);
        }
    }
}

