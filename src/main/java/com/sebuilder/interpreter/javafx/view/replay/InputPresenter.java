package com.sebuilder.interpreter.javafx.view.replay;

import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.javafx.application.ReplayOption;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.view.data.DataSetView;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InputPresenter {

    static private final Pair<Integer, InputData> DEFAULT = new Pair<>(1, new InputData());
    @Inject
    private SeInterpreterApplication application;
    @FXML
    private TableView<InputResource> inputResourceTableView;
    @FXML
    private TableColumn<InputResource, String> resourceName;
    @FXML
    private TableColumn<InputResource, Integer> row;
    @FXML
    private TableColumn<InputResource, String> slash;
    @FXML
    private TableColumn<InputResource, Integer> rows;
    @FXML
    private TableColumn<InputResource, Void> buttonOpen;
    @FXML
    private TableColumn<InputResource, Void> buttonEdit;

    private Map<String, Pair<Integer, InputData>> shareInputs = new HashMap<>();

    private Consumer<ReplayOption> onclickReplayStart;

    @FXML
    void initialize() {
        this.onclickReplayStart = (it) -> application.runScript(it);
        this.resourceName.setCellValueFactory(body -> body.getValue().resourceNameProperty());
        this.row.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        this.row.setCellValueFactory(body -> body.getValue().rowProperty().asObject());
        this.slash.setCellValueFactory(body -> new SimpleStringProperty("/"));
        this.rows.setCellValueFactory(body -> body.getValue().rowsProperty().asObject());
        this.buttonOpen.setCellFactory(inputResourceStringTableColumn -> new TableCell<>() {
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button btn = new Button("open");
                    btn.setOnAction((ActionEvent event) -> {
                        InputResource data = getTableView().getItems().get(getIndex());
                        DataSetView dataSetView = new DataSetView();
                        dataSetView.onClick(e -> refreshTable());
                        dataSetView.showDataSet(data.getLoader(), inputResourceTableView.getScene().getWindow());
                    });
                    setGraphic(btn);
                }
            }
        });
        this.buttonEdit.setCellFactory(inputResourceStringTableColumn -> new TableCell<>() {
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button btn = new Button("edit");
                    btn.setOnAction((ActionEvent event) -> {
                        InputResource data = getTableView().getItems().get(getIndex());
                        VariableView variableView = new VariableView();
                        variableView.onClick(result -> {
                            data.setRuntimeVariable(result);
                            shareInputs.put(data.getResourceName(), new Pair<>(data.getRow(), data.getRuntimeVariable()));
                            refreshTable();
                        });
                        variableView.open(data.getRuntimeVariable(), inputResourceTableView.getScene().getWindow());
                    });
                    setGraphic(btn);
                }
            }
        });
        this.refreshTable();
    }

    @FXML
    public void handleReplayStart() {
        this.onclickReplayStart.accept(this.createReplayOption());
    }

    public void setOnclickReplayStart(Consumer<ReplayOption> onclickReplayStart) {
        this.onclickReplayStart = onclickReplayStart;
    }

    private void refreshTable() {
        this.inputResourceTableView.getItems().setAll(new ArrayList<>());
        for (DataSourceLoader loadable : this.createReplayOption().filterLoadableSource(this.application.replayShareInput(), this.application.getDisplayTestCaseDataSources())) {
            this.inputResourceTableView.getItems().add(new InputResource(loadable));
        }
        this.inputResourceTableView.refresh();
    }

    private ReplayOption createReplayOption() {
        return new ReplayOption(this.shareInputs);
    }

    class InputResource {
        private final DataSourceLoader loader;
        private InputData runtimeVariable;
        private StringProperty resourceName;
        private IntegerProperty row;
        private IntegerProperty rows;

        public InputResource(DataSourceLoader loader) {
            this.loader = loader;
            List<InputData> data = this.loader.loadData();
            this.resourceName = new SimpleStringProperty(this.loader.name());
            if (!shareInputs.containsKey(this.getResourceName())) {
                shareInputs.put(this.getResourceName(), DEFAULT);
            }
            this.runtimeVariable = shareInputs.get(this.resourceName.get()).getValue();
            this.row = new SimpleIntegerProperty(shareInputs.get(this.resourceName.get()).getKey());
            this.row.addListener((observableValue, oldVal, newVal) -> {
                setRuntimeVariable(new InputData());
                shareInputs.put(resourceName.get(), new Pair<>(observableValue.getValue().intValue(), getRuntimeVariable()));
                refreshTable();
            });
            this.rows = new SimpleIntegerProperty(data.size());
        }

        public DataSourceLoader getLoader() {
            return loader;
        }

        public String getResourceName() {
            return resourceName.get();
        }

        public StringProperty resourceNameProperty() {
            return resourceName;
        }

        public int getRow() {
            return row.get();
        }

        public IntegerProperty rowProperty() {
            return row;
        }

        public InputData getRuntimeVariable() {
            return runtimeVariable;
        }

        public void setRuntimeVariable(InputData runtimeVariable) {
            this.runtimeVariable = runtimeVariable;
        }

        public void setRow(int row) {
            this.row.set(row);
        }

        public int getRows() {
            return rows.get();
        }

        public IntegerProperty rowsProperty() {
            return rows;
        }
    }
}
