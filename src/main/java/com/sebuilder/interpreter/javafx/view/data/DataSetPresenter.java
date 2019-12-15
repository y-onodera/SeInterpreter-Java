package com.sebuilder.interpreter.javafx.view.data;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
import com.sebuilder.interpreter.javafx.control.ExcelLikeSpreadSheetView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sebuilder.interpreter.javafx.control.ExcelLikeSpreadSheetView.TEXT_AREA;

public class DataSetPresenter {

    private static final int DEFAULT_ROWS = 50;
    private static final int DEFAULT_COLUMNS = 50;
    @Inject
    private SeInterpreterApplication application;
    @FXML
    private AnchorPane gridParentPane;

    private SpreadsheetView sheet;

    public void showDataSet(DataSourceLoader resource) {
        List<InputData> inputData = resource.loadData();
        int row = inputData.size() < DEFAULT_ROWS ? DEFAULT_ROWS : inputData.size();
        int column = inputData.size() < 1 || inputData.get(0).input().size() < DEFAULT_COLUMNS ? DEFAULT_COLUMNS : inputData.get(0).input().size();
        GridBase grid = new GridBase(row, column);
        ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
        inputData.forEach(it -> {
            if (Integer.parseInt(it.rowNumber()) == 1) {
                addRow(rows, 0, it, Map.Entry::getKey, cell -> {
                    cell.getStyleClass().add("header");
                    return cell;
                });
            }
            addRow(rows, Integer.parseInt(it.rowNumber()), it, Map.Entry::getValue);
        });
        if (rows.size() < DEFAULT_ROWS) {
            for (int current = rows.size(); current < DEFAULT_ROWS; current++) {
                addRow(rows, current, new InputData(), Map.Entry::getValue);
            }
        }
        grid.setRows(rows);
        sheet = new ExcelLikeSpreadSheetView(grid);
        sheet.getFixedRows().add(Integer.valueOf(0));
        AnchorPane.setTopAnchor(sheet, Double.valueOf(0));
        AnchorPane.setBottomAnchor(sheet, Double.valueOf(0));
        AnchorPane.setRightAnchor(sheet, Double.valueOf(0));
        AnchorPane.setLeftAnchor(sheet, Double.valueOf(0));
        this.gridParentPane.getChildren().add(sheet);
    }

    @FXML
    void reloadDataSet(ActionEvent actionEvent) {
        this.gridParentPane.getChildren().clear();
        this.showDataSet(this.application.getDisplayTestCase().runtimeDataSet());
    }

    @FXML
    public void saveDataSet(ActionEvent actionEvent) {
        ObservableList<ObservableList<SpreadsheetCell>> rows = this.sheet.getGrid().getRows();
        List<Pair<Integer, String>> header = rows.get(0)
                .stream()
                .filter(it -> !Strings.isNullOrEmpty(it.getText()))
                .map(it -> new Pair<>(Integer.valueOf(it.getColumn()), it.getText()))
                .collect(Collectors.toList());
        ArrayList<InputData> saveContents = rows.subList(1, rows.size() - 1).stream()
                .filter(it -> hasValue(it, header))
                .map(it -> toTestData(it, header))
                .collect(Collectors.toCollection(ArrayList::new));
        if (saveContents.size() > 0) {
            this.application.executeAndLoggingCaseWhenThrowException(() -> this.application.getDisplayTestCase()
                    .runtimeDataSet()
                    .writer()
                    .write(saveContents));
        }
        this.reloadDataSet(actionEvent);
    }

    protected InputData toTestData(ObservableList<SpreadsheetCell> row, List<Pair<Integer, String>> header) {
        return header.stream()
                .map(it -> {
                    String value = "";
                    if (isExistsCell(row, it)) {
                        value = row.get(it.getKey()).getText();
                    }
                    return new InputData().add(it.getValue(), value);
                })
                .reduce(new InputData(), InputData::add);
    }

    protected boolean hasValue(ObservableList<SpreadsheetCell> row, List<Pair<Integer, String>> header) {
        return header.stream().anyMatch(it -> isExistsCell(row, it) && !Strings.isNullOrEmpty(row.get(it.getKey()).getText()));
    }

    protected void addRow(ObservableList<ObservableList<SpreadsheetCell>> rows, int row, InputData it, Function<Map.Entry<String, String>, String> map) {
        this.addRow(rows, row, it, map, cell -> cell);
    }

    protected void addRow(ObservableList<ObservableList<SpreadsheetCell>> rows, int row, InputData it, Function<Map.Entry<String, String>, String> map, Function<SpreadsheetCell, SpreadsheetCell> setStyle) {
        final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
        int col = 0;
        for (Map.Entry<String, String> entry : it.input()) {
            SpreadsheetCell cell = TEXT_AREA.createCell(row, col, 1, 1, map.apply(entry));
            dataRow.add(setStyle.apply(cell));
            col++;
        }
        for (; col < DEFAULT_COLUMNS; col++) {
            SpreadsheetCell cell = TEXT_AREA.createCell(row, col, 1, 1, null);
            dataRow.add(setStyle.apply(cell));
        }
        rows.add(dataRow);
    }

    private boolean isExistsCell(ObservableList<SpreadsheetCell> row, Pair<Integer, String> it) {
        return row.size() > it.getKey() && row.get(it.getKey()) != null;
    }
}
