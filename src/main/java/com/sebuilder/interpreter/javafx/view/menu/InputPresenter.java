package com.sebuilder.interpreter.javafx.view.menu;

import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.InputData;
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
import javafx.util.converter.IntegerStringConverter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputPresenter {

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
    private TableColumn<InputResource, Void> button;

    private Map<String, Integer> shareInputs = new HashMap<>();

    @FXML
    void initialize() {
        this.resourceName.setCellValueFactory(body -> body.getValue().resourceNameProperty());
        this.row.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        this.row.setCellValueFactory(body -> body.getValue().rowProperty().asObject());
        this.slash.setCellValueFactory(body -> new SimpleStringProperty("/"));
        this.rows.setCellValueFactory(body -> body.getValue().rowsProperty().asObject());
        this.button.setCellFactory(inputResourceStringTableColumn -> new TableCell<InputResource, Void>() {
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
        this.refreshTable();
    }

    @FXML
    public void handleReplayStart(ActionEvent actionEvent) {
        this.application.runScript(this.shareInputs);
    }

    private void refreshTable() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.inputResourceTableView.getItems().setAll(new ArrayList<>());
            InputData shareInput = this.application.replayShareInput();
            for (DataSourceLoader loader : this.application.getDisplayTestCaseDataSources()) {
                DataSourceLoader withShareInput = loader.shareInput(shareInput);
                if (withShareInput.isLoadable()) {
                    InputResource row = new InputResource(withShareInput);
                    this.inputResourceTableView.getItems().add(row);
                    shareInput = shareInput.add(withShareInput.loadData().get(row.getRow() - 1));
                }
            }
            this.inputResourceTableView.refresh();
        });
    }

    class InputResource {
        private final DataSourceLoader loader;
        private StringProperty resourceName;
        private IntegerProperty row;
        private IntegerProperty rows;

        public InputResource(DataSourceLoader loader) {
            this.loader = loader;
            List<InputData> data = this.loader.loadData();
            this.resourceName = new SimpleStringProperty(this.loader.name());
            this.row = new SimpleIntegerProperty(shareInputs.getOrDefault(this.resourceName.get(), 1));
            final String key = this.resourceName.get();
            this.row.addListener((observableValue, oldVal, newVal) -> {
                shareInputs.put(key, observableValue.getValue().intValue());
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
