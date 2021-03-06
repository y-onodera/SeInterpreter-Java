package com.sebuilder.interpreter.javafx.view.replay;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.javafx.application.ReplayOption;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.view.data.DataSetView;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Pair;
import javafx.util.converter.IntegerStringConverter;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InputPresenter {

    static private final Pair<Integer, InputData> DEFAULT = new Pair<>(1, new InputData());
    public static final DataSourceLoader NO_DATASOURCE = new DataSourceLoader(DataSource.NONE, Maps.newHashMap(), null);
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
    @FXML
    private CheckBox aspectTakeOver;

    private Map<String, Pair<Integer, InputData>> shareInputs = new HashMap<>();

    private Consumer<ReplayOption> onclickReplayStart;

    @FXML
    void initialize() {
        this.onclickReplayStart = (it) -> application.runScript(it);
        this.resourceName.setCellValueFactory(body -> body.getValue().resourceNameProperty());
        this.row.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        this.row.setCellValueFactory(body -> body.getValue().rowValue());
        this.slash.setCellValueFactory(body -> new SimpleStringProperty("/"));
        this.rows.setCellValueFactory(body -> body.getValue().rowsValue());
        this.buttonOpen.setCellFactory(inputResourceStringTableColumn -> new TableCell<>() {
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(getTableView().getItems().get(getIndex()).createOpenButton());
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
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            for (DataSourceLoader loadable : this.createReplayOption().filterLoadableSource(this.application.replayShareInput(), this.application.getDisplayTestCaseDataSources())) {
                this.inputResourceTableView.getItems().add(new InputResource(loadable));
            }
            if (this.inputResourceTableView.getItems().size() == 0) {
                this.inputResourceTableView.getItems().add(new InputResource());
            }
            this.inputResourceTableView.refresh();
        });
    }

    private ReplayOption createReplayOption() {
        return new ReplayOption(this.shareInputs, this.aspectTakeOver.isSelected());
    }

    class InputResource {
        private final DataSourceLoader loader;
        private InputData runtimeVariable;
        private StringProperty resourceName;
        private IntegerProperty row;
        private IntegerProperty rows;

        public InputResource() throws IOException {
            this(NO_DATASOURCE);
        }

        public InputResource(DataSourceLoader loader) throws IOException {
            this.loader = loader;
            List<InputData> data = this.loader.loadData();
            this.resourceName = new SimpleStringProperty(this.loader.name());
            if (!shareInputs.containsKey(this.getResourceName())) {
                shareInputs.put(this.getResourceName(), DEFAULT);
            }
            this.runtimeVariable = shareInputs.get(this.resourceName.get()).getValue();
            if (this.loader != NO_DATASOURCE) {
                this.row = new SimpleIntegerProperty(shareInputs.get(this.resourceName.get()).getKey());
                this.row.addListener((observableValue, oldVal, newVal) -> {
                    setRuntimeVariable(new InputData());
                    shareInputs.put(resourceName.get(), new Pair<>(observableValue.getValue().intValue(), getRuntimeVariable()));
                    refreshTable();
                });
                this.rows = new SimpleIntegerProperty(data.size());
            }
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
            if (this.loader == NO_DATASOURCE) {
                return 1;
            }
            return row.get();
        }

        public ObservableValue<Integer> rowValue() {
            if (this.loader == NO_DATASOURCE) {
                return null;
            }
            return this.rowProperty().asObject();
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

        public IntegerProperty rowsProperty() {
            return rows;
        }

        public ObservableValue<Integer> rowsValue() {
            if (this.loader == NO_DATASOURCE) {
                return null;
            }
            return this.rowsProperty().asObject();
        }

        public Button createOpenButton() {
            if (this.loader == NO_DATASOURCE) {
                return null;
            }
            Button btn = new Button("open");
            btn.setOnAction((ActionEvent event) -> {
                InputResource data = this;
                application.executeAndLoggingCaseWhenThrowException(() -> {
                    DataSetView dataSetView = new DataSetView();
                    dataSetView.onClick(e -> refreshTable());
                    dataSetView.showDataSet(data.getLoader(), inputResourceTableView.getScene().getWindow());
                });
            });
            return btn;
        }
    }
}
