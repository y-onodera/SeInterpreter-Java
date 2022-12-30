package com.sebuilder.interpreter.javafx.view.replay;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.javafx.control.ExcelLikeSpreadSheetView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.util.Map;
import java.util.function.Consumer;

import static com.sebuilder.interpreter.javafx.control.ExcelLikeSpreadSheetView.TEXT_AREA;

public class VariablePresenter {
    @FXML
    private AnchorPane gridParentPane;

    private SpreadsheetView sheet;

    private Consumer<InputData> onclick;

    private InputData resource;

    public void open(final InputData var) {
        this.resource = var;
        final GridBase grid = new GridBase(50, 2);
        grid.getColumnHeaders().addAll("key", "value");
        final ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
        int i = 0;
        for (final Map.Entry<String, String> entry : var.input().entrySet()) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            dataRow.add(TEXT_AREA.createCell(i, 0, 1, 1, entry.getKey()));
            dataRow.add(TEXT_AREA.createCell(i, 1, 1, 1, entry.getValue()));
            rows.add(dataRow);
            i++;
        }
        for (final int j = 50; i < j; i++) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            dataRow.add(TEXT_AREA.createCell(i, 0, 1, 1, ""));
            dataRow.add(TEXT_AREA.createCell(i, 1, 1, 1, ""));
            rows.add(dataRow);
        }
        grid.setRows(rows);
        this.sheet = new ExcelLikeSpreadSheetView(grid);
        this.sheet.getColumns().forEach(it -> it.setMinWidth(175.0));
        AnchorPane.setTopAnchor(this.sheet, 0.0);
        AnchorPane.setBottomAnchor(this.sheet, 0.0);
        AnchorPane.setRightAnchor(this.sheet, 0.0);
        AnchorPane.setLeftAnchor(this.sheet, 0.0);
        this.gridParentPane.getChildren().add(this.sheet);
    }

    @FXML
    void save() {
        this.resource = this.sheet.getGrid().getRows()
                .stream()
                .filter(it -> !Strings.isNullOrEmpty(it.get(0).getText()))
                .reduce(new InputData()
                        , (result, row) -> result.add(row.get(0).getText(), row.get(1).getText())
                        , InputData::add);
        this.reload();
        this.onclick.accept(this.resource);
    }

    @FXML
    void reload() {
        this.gridParentPane.getChildren().clear();
        this.open(this.resource);
    }

    public void setOnclick(final Consumer<InputData> onclick) {
        this.onclick = onclick;
    }

}
