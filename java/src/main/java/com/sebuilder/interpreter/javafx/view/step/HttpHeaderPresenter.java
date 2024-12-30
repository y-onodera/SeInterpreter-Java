package com.sebuilder.interpreter.javafx.view.step;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.javafx.control.spreadsheet.ExcelLikeSpreadSheetView;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.openqa.selenium.bidi.network.BytesValue;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.sebuilder.interpreter.javafx.control.spreadsheet.ExcelLikeSpreadSheetView.TEXT_AREA;

public class HttpHeaderPresenter {
    public static final SpreadsheetCellType.ListType TYPE_SELECT =
            new SpreadsheetCellType.ListType(Stream.concat(Stream.of(""), Arrays.stream(BytesValue.Type.values())
                    .map(BytesValue.Type::name)).toList());
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private AnchorPane gridParentPane;

    private SpreadsheetView sheet;

    private Consumer<HttpHeaders> onclick;

    private HttpHeaders resource;

    void populate(final HttpHeaders var) {
        this.resource = var;
        final GridBase grid = new GridBase(50, 3);
        grid.getColumnHeaders().addAll("key", "type", "value");
        final ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
        int i = 0;
        for (final Map.Entry<String, BytesValue> entry : this.resource.params().entrySet()) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            dataRow.add(TEXT_AREA.createCell(i, 0, 1, 1, entry.getKey()));
            dataRow.add(TYPE_SELECT.createCell(i, 1, 1, 1, entry.getValue().getType().name()));
            dataRow.add(TEXT_AREA.createCell(i, 2, 1, 1, entry.getValue().getValue()));
            rows.add(dataRow);
            i++;
        }
        for (final int j = 50; i < j; i++) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            dataRow.add(TEXT_AREA.createCell(i, 0, 1, 1, ""));
            dataRow.add(TYPE_SELECT.createCell(i, 1, 1, 1, ""));
            dataRow.add(TEXT_AREA.createCell(i, 2, 1, 1, ""));
            rows.add(dataRow);
        }
        grid.setRows(rows);
        this.sheet = new ExcelLikeSpreadSheetView(grid);
        this.sheet.getColumns().get(0).setMinWidth(175);
        this.sheet.getColumns().get(1).setMinWidth(100);
        this.sheet.getColumns().get(2).setMinWidth(175);
        AnchorPane.setTopAnchor(this.sheet, 0.0);
        AnchorPane.setBottomAnchor(this.sheet, 0.0);
        AnchorPane.setRightAnchor(this.sheet, 0.0);
        AnchorPane.setLeftAnchor(this.sheet, 0.0);
        this.gridParentPane.getChildren().add(this.sheet);
    }

    @FXML
    void save() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.resource = this.sheet.getGrid().getRows()
                    .stream()
                    .filter(it -> !Strings.isNullOrEmpty(it.get(0).getText()))
                    .reduce(new HttpHeaders()
                            , (result, row) -> result.add(row.get(0).getText()
                                    , row.get(1).getText()
                                    , row.get(2).getText())
                            , HttpHeaders::add);
            this.reload();
            this.onclick.accept(this.resource);
        });
    }

    @FXML
    void reload() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            this.gridParentPane.getChildren().clear();
            this.populate(this.resource);
        });
    }

    void setOnclick(final Consumer<HttpHeaders> onclick) {
        this.onclick = onclick;
    }

}
