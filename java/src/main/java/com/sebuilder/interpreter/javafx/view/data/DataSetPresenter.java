package com.sebuilder.interpreter.javafx.view.data;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.control.ExcelLikeSpreadSheetView;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sebuilder.interpreter.javafx.control.ExcelLikeSpreadSheetView.TEXT_AREA;

public class DataSetPresenter {

    private static final int DEFAULT_ROWS = 50;
    private static final int DEFAULT_COLUMNS = 50;
    @Inject
    private SeInterpreterApplication application;
    @FXML
    private AnchorPane gridParentPane;

    private DataSourceLoader resource;

    private SpreadsheetView sheet;

    private EventHandler<ActionEvent> onclick;

    public void showDataSet(final DataSourceLoader resource) throws IOException {
        this.resource = resource;
        final List<InputData> inputData = this.resource.loadData();
        final int row = Math.max(inputData.size(), DEFAULT_ROWS);
        final int column = inputData.size() < 1 || inputData.get(0).input().size() < DEFAULT_COLUMNS ? DEFAULT_COLUMNS : inputData.get(0).input().size();
        final GridBase grid = new GridBase(row, column);
        final ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
        inputData.forEach(it -> {
            if (Integer.parseInt(it.rowNumber()) == 1) {
                this.addRow(rows, 0, column, it, Map.Entry::getKey, cell -> {
                    cell.getStyleClass().add("header");
                    return cell;
                });
            }
            this.addRow(rows, Integer.parseInt(it.rowNumber()), column, it, Map.Entry::getValue);
        });
        if (rows.size() < DEFAULT_ROWS) {
            for (int current = rows.size(); current < DEFAULT_ROWS; current++) {
                this.addRow(rows, current, column, new InputData(), Map.Entry::getValue);
            }
        }
        grid.setRows(rows);
        this.sheet = new ExcelLikeSpreadSheetView(grid);
        this.sheet.getColumns().forEach(it -> it.setMinWidth(175.0));
        this.sheet.getFixedRows().add(0);
        AnchorPane.setTopAnchor(this.sheet, 0.0);
        AnchorPane.setBottomAnchor(this.sheet, 0.0);
        AnchorPane.setRightAnchor(this.sheet, 0.0);
        AnchorPane.setLeftAnchor(this.sheet, 0.0);
        this.gridParentPane.getChildren().add(this.sheet);
    }

    public void setOnclick(final EventHandler<ActionEvent> onclick) {
        this.onclick = onclick;
    }

    @FXML
    void reloadDataSet() {
        this.application.executeAndLoggingCaseWhenThrowException(() -> {
            this.gridParentPane.getChildren().clear();
            this.showDataSet(this.resource);
        });
    }

    @FXML
    public void saveDataSet(final ActionEvent actionEvent) {
        final ObservableList<ObservableList<SpreadsheetCell>> rows = this.sheet.getGrid().getRows();
        final List<Pair<Integer, String>> header = rows.get(0)
                .stream()
                .filter(it -> !Strings.isNullOrEmpty(it.getText()))
                .map(it -> new Pair<>(it.getColumn(), it.getText()))
                .collect(Collectors.toList());
        final ArrayList<InputData> saveContents = rows.subList(1, rows.size() - 1).stream()
                .filter(it -> this.hasValue(it, header))
                .map(it -> this.toTestData(it, header))
                .collect(Collectors.toCollection(ArrayList::new));
        if (saveContents.size() > 0) {
            this.application.executeAndLoggingCaseWhenThrowException(() -> {
                this.resource.writer().write(saveContents);
                SuccessDialog.show("save succeed");
                this.reloadDataSet();
                if (this.onclick != null) {
                    this.onclick.handle(actionEvent);
                }
            });
        }
    }

    protected InputData toTestData(final ObservableList<SpreadsheetCell> row, final List<Pair<Integer, String>> header) {
        return header.stream()
                .map(it -> {
                    String value = "";
                    if (this.isExistsCell(row, it)) {
                        value = row.get(it.getKey()).getText();
                    }
                    return new InputData().add(it.getValue(), value);
                })
                .reduce(new InputData(), InputData::add);
    }

    protected boolean hasValue(final ObservableList<SpreadsheetCell> row, final List<Pair<Integer, String>> header) {
        return header.stream().anyMatch(it -> this.isExistsCell(row, it) && !Strings.isNullOrEmpty(row.get(it.getKey()).getText()));
    }

    protected void addRow(final ObservableList<ObservableList<SpreadsheetCell>> rows, final int row, final int column, final InputData it, final Function<Map.Entry<String, String>, String> map) {
        this.addRow(rows, row, column, it, map, cell -> cell);
    }

    protected void addRow(final ObservableList<ObservableList<SpreadsheetCell>> rows, final int row, final int column, final InputData it, final Function<Map.Entry<String, String>, String> map, final Function<SpreadsheetCell, SpreadsheetCell> setStyle) {
        final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
        int col = 0;
        for (final Map.Entry<String, String> entry : it.input().entrySet()) {
            final SpreadsheetCell cell = TEXT_AREA.createCell(row, col, 1, 1, map.apply(entry));
            dataRow.add(setStyle.apply(cell));
            col++;
        }
        for (; col < column; col++) {
            final SpreadsheetCell cell = TEXT_AREA.createCell(row, col, 1, 1, null);
            dataRow.add(setStyle.apply(cell));
        }
        rows.add(dataRow);
    }

    private boolean isExistsCell(final ObservableList<SpreadsheetCell> row, final Pair<Integer, String> it) {
        return row.size() > it.getKey() && row.get(it.getKey()) != null;
    }
}
