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
import javafx.stage.Window;
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

    private final Map<String, Pair<Integer, InputData>> shareInputs = new HashMap<>();

    private Consumer<ReplayOption> onclickReplayStart;

    void setOnclickReplayStart(final Consumer<ReplayOption> onclickReplayStart) {
        this.onclickReplayStart = onclickReplayStart;
    }

    @FXML
    void initialize() {
        this.onclickReplayStart = (it) -> this.application.runScript(it);
        this.resourceName.setCellValueFactory(body -> body.getValue().resourceNameProperty());
        this.row.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        this.row.setCellValueFactory(body -> body.getValue().rowValue());
        this.slash.setCellValueFactory(body -> new SimpleStringProperty("/"));
        this.rows.setCellValueFactory(body -> body.getValue().rowsValue());
        this.buttonOpen.setCellFactory(inputResourceStringTableColumn -> new TableCell<>() {
            @Override
            public void updateItem(final Void item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    this.setGraphic(null);
                } else {
                    this.setGraphic(this.getTableView().getItems().get(this.getIndex()).createOpenButton());
                }
            }
        });
        this.buttonEdit.setCellFactory(inputResourceStringTableColumn -> new TableCell<>() {
            @Override
            public void updateItem(final Void item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    this.setGraphic(null);
                } else {
                    final Button btn = new Button("edit");
                    btn.setOnAction((final ActionEvent event) -> {
                        final InputResource data = this.getTableView().getItems().get(this.getIndex());
                        VariableView.builder()
                                .setTitle("runtime variable")
                                .setOnclick(result -> {
                                    data.setRuntimeVariable(result);
                                    InputPresenter.this.shareInputs.put(data.getResourceName(), new Pair<>(data.getRow(), data.getRuntimeVariable()));
                                    InputPresenter.this.refreshTable();
                                })
                                .setTarget(data.getRuntimeVariable())
                                .setWindow(InputPresenter.this.currentWindow())
                                .build();
                    });
                    this.setGraphic(btn);
                }
            }
        });
        this.refreshTable();
    }

    @FXML
    void handleReplayStart() {
        this.onclickReplayStart.accept(this.createReplayOption());
    }

    private void refreshTable() {
        this.inputResourceTableView.getItems().setAll(new ArrayList<>());
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            for (final DataSourceLoader loadable : this.createReplayOption().filterLoadableSource(this.application.replayShareInput(), this.application.getDisplayTestCaseDataSources())) {
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

    private Window currentWindow() {
        return InputPresenter.this.inputResourceTableView.getScene().getWindow();
    }

    class InputResource {
        private final DataSourceLoader loader;
        private InputData runtimeVariable;
        private final StringProperty resourceName;
        private IntegerProperty row;
        private IntegerProperty rows;

        public InputResource() throws IOException {
            this(NO_DATASOURCE);
        }

        public InputResource(final DataSourceLoader loader) throws IOException {
            this.loader = loader;
            final List<InputData> data = this.loader.loadData();
            this.resourceName = new SimpleStringProperty(this.loader.name());
            if (!InputPresenter.this.shareInputs.containsKey(this.getResourceName())) {
                InputPresenter.this.shareInputs.put(this.getResourceName(), DEFAULT);
            }
            this.runtimeVariable = InputPresenter.this.shareInputs.get(this.resourceName.get()).getValue();
            if (this.loader != NO_DATASOURCE) {
                this.row = new SimpleIntegerProperty(InputPresenter.this.shareInputs.get(this.resourceName.get()).getKey());
                this.row.addListener((observableValue, oldVal, newVal) -> {
                    this.setRuntimeVariable(new InputData());
                    InputPresenter.this.shareInputs.put(this.resourceName.get(), new Pair<>(observableValue.getValue().intValue(), this.getRuntimeVariable()));
                    InputPresenter.this.refreshTable();
                });
                this.rows = new SimpleIntegerProperty(data.size());
            }
        }

        public DataSourceLoader getLoader() {
            return this.loader;
        }

        public String getResourceName() {
            return this.resourceName.get();
        }

        public StringProperty resourceNameProperty() {
            return this.resourceName;
        }

        public int getRow() {
            if (this.loader == NO_DATASOURCE) {
                return 1;
            }
            return this.row.get();
        }

        public ObservableValue<Integer> rowValue() {
            if (this.loader == NO_DATASOURCE) {
                return null;
            }
            return this.rowProperty().asObject();
        }

        public IntegerProperty rowProperty() {
            return this.row;
        }

        public InputData getRuntimeVariable() {
            return this.runtimeVariable;
        }

        public void setRuntimeVariable(final InputData runtimeVariable) {
            this.runtimeVariable = runtimeVariable;
        }

        public void setRow(final int row) {
            this.row.set(row);
        }

        public IntegerProperty rowsProperty() {
            return this.rows;
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
            final Button btn = new Button("open");
            btn.setOnAction((final ActionEvent event) -> {
                final InputResource data = this;
                InputPresenter.this.application.executeAndLoggingCaseWhenThrowException(() -> {
                    final DataSetView dataSetView = new DataSetView();
                    dataSetView.onClick(e -> InputPresenter.this.refreshTable());
                    dataSetView.open(data.getLoader(), InputPresenter.this.currentWindow());
                });
            });
            return btn;
        }
    }
}
