package com.sebuilder.interpreter.javafx.view.suite;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestData;
import com.sebuilder.interpreter.javafx.application.SeInterpreterApplication;
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

import static com.sebuilder.interpreter.javafx.view.suite.PlanTextCopyPasteAbleSpreadSheetView.TEXT_AREA;

public class DataSetPresenter {

    @Inject
    private SeInterpreterApplication application;
    @FXML
    private AnchorPane gridParentPane;

    private SpreadsheetView sheet;

    public void showDataSet(TestCase currentCase) {
        List<TestData> testData = currentCase.loadData();
        int row = testData.size() < 50 ? 50 : testData.size();
        int column = testData.size() < 1 || testData.get(0).input().size() < 50 ? 50 : testData.get(0).input().size();
        GridBase grid = new GridBase(row, column);
        ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
        testData.forEach(it -> {
            if (Integer.parseInt(it.rowNumber()) == 1) {
                addRow(rows, 0, it, Map.Entry::getKey, cell -> {
                    cell.getStyleClass().add("header");
                    return cell;
                });
            }
            addRow(rows, Integer.parseInt(it.rowNumber()), it, Map.Entry::getValue);
        });
        if (rows.size() < 50) {
            for (int current = rows.size(); current < 50; current++) {
                addRow(rows, current, new TestData(), Map.Entry::getValue);
            }
        }
        grid.setRows(rows);
        sheet = new PlanTextCopyPasteAbleSpreadSheetView(grid);
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
        this.showDataSet(this.application.getDisplayTestCase());
    }

    @FXML
    public void saveDataSet(ActionEvent actionEvent) {
        ObservableList<ObservableList<SpreadsheetCell>> rows = this.sheet.getGrid().getRows();
        List<Pair<Integer, String>> header = rows.get(0)
                .stream()
                .filter(it -> !Strings.isNullOrEmpty(it.getText()))
                .map(it -> new Pair<>(Integer.valueOf(it.getColumn()), it.getText()))
                .collect(Collectors.toList());
        ArrayList<TestData> saveContents = rows.subList(1, rows.size() - 1).stream()
                .filter(it -> hasValue(it, header))
                .map(it -> toTestData(it, header))
                .collect(Collectors.toCollection(ArrayList::new));
        if (saveContents.size() > 0) {
            this.application.executeAndLoggingCaseWhenThrowException(() -> this.application.getDisplayTestCase().runtimeDataSetWriter().writer(saveContents));
        }
        this.reloadDataSet(actionEvent);
    }

    protected TestData toTestData(ObservableList<SpreadsheetCell> row, List<Pair<Integer, String>> header) {
        return header.stream()
                .map(it -> {
                    String value = "";
                    if (row.size() > it.getKey() && row.get(it.getKey()) != null) {
                        value = row.get(it.getKey()).getText();
                    }
                    return new TestData().add(it.getValue(), value);
                })
                .reduce(new TestData(), TestData::add);
    }

    protected boolean hasValue(ObservableList<SpreadsheetCell> row, List<Pair<Integer, String>> header) {
        return header.stream().anyMatch(it -> row.size() > it.getKey() && row.get(it.getKey()) != null && !Strings.isNullOrEmpty(row.get(it.getKey()).getText()));
    }

    protected void addRow(ObservableList<ObservableList<SpreadsheetCell>> rows, int row, TestData it, Function<Map.Entry<String, String>, String> map) {
        this.addRow(rows, row, it, map, (SpreadsheetCell cell) -> cell);
    }

    protected void addRow(ObservableList<ObservableList<SpreadsheetCell>> rows, int row, TestData it, Function<Map.Entry<String, String>, String> map, Function<SpreadsheetCell, SpreadsheetCell> setStyle) {
        final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
        int col = 0;
        for (Map.Entry<String, String> entry : it.input()) {
            SpreadsheetCell cell = TEXT_AREA.createCell(row, col, 1, 1, map.apply(entry));
            dataRow.add(setStyle.apply(cell));
            col++;
        }
        for (; col < 50; col++) {
            SpreadsheetCell cell = TEXT_AREA.createCell(row, col, 1, 1, null);
            dataRow.add(setStyle.apply(cell));
        }
        rows.add(dataRow);
    }

}
