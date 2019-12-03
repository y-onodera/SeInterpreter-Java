package com.sebuilder.interpreter.javafx.view.suite;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import org.controlsfx.control.spreadsheet.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class PlanTextCopyPasteAbleSpreadSheetView extends org.controlsfx.control.spreadsheet.SpreadsheetView {

    public static final SpreadsheetCellType.StringType TEXT_AREA = new SpreadsheetCellType.StringType() {
        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            return new SpreadsheetCellEditor.TextAreaEditor(view);
        }
    };

    private DataFormat spreadSheetViewFmt;

    public PlanTextCopyPasteAbleSpreadSheetView(GridBase grid) {
        super(grid);
    }

    @Override
    public void copyClipboard() {
        super.copyClipboard();
        Object content = Clipboard.getSystemClipboard().getContent(this.getSpreadSheetDataFormat());
        if (content != null) {
            ClipboardContent newContent = new ClipboardContent();
            newContent.put(this.getSpreadSheetDataFormat(), content);
            newContent.putString(this.toString((ArrayList<ClipboardCell>) content));
            Clipboard.getSystemClipboard().setContent(newContent);
        }
    }

    @Override
    public void pasteClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (this.isPasteOutsideApplication(clipboard)) {
            ClipboardContent newContent = new ClipboardContent();
            newContent.put(this.getSpreadSheetDataFormat(), this.toClipboardCell(clipboard.getContent(DataFormat.PLAIN_TEXT).toString()));
            Clipboard.getSystemClipboard().setContent(newContent);
        }
        super.pasteClipboard();
    }

    protected boolean isPasteOutsideApplication(Clipboard clipboard) {
        return clipboard.getContent(DataFormat.PLAIN_TEXT) != null && clipboard.getContent(getSpreadSheetDataFormat()) == null;
    }

    protected String toString(ArrayList<ClipboardCell> content) {
        Map<Integer, StringBuilder> copy = Maps.newLinkedHashMap();
        for (ClipboardCell cell : content) {
            copy.merge(cell.getRow()
                    , new StringBuilder(cell.getValue() == null ? "" : "\"" + cell.getValue() + "\"")
                    , (summary, newVal) -> summary.append("\t").append(newVal));
        }
        StringJoiner result = new StringJoiner("\r\n");
        copy.values().forEach(it -> result.add(it));
        return result.toString();
    }

    protected List<ClipboardCell> toClipboardCell(String content) {
        int rowNo = 0;
        List<ClipboardCell> pasteContents = Lists.newArrayList();
        CSVReader csvR = new CSVReaderBuilder(new StringReader(content)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
        for (String[] row : csvR) {
            int columnNo = 0;
            for (String column : row) {
                SpreadsheetCell cell = TEXT_AREA.createCell(rowNo, columnNo, 1, 1, column);
                pasteContents.add(new ClipboardCell(rowNo, columnNo, cell));
                columnNo++;
            }
            rowNo++;
        }
        return pasteContents;
    }

    private DataFormat getSpreadSheetDataFormat() {
        if ((this.spreadSheetViewFmt = DataFormat.lookupMimeType("SpreadsheetView")) == null) {
            this.spreadSheetViewFmt = new DataFormat(new String[]{"SpreadsheetView"});
        }
        return this.spreadSheetViewFmt;
    }
}
