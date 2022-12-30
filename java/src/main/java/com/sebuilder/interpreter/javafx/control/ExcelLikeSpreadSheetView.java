package com.sebuilder.interpreter.javafx.control;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import org.controlsfx.control.spreadsheet.*;

import java.io.StringReader;
import java.util.*;

public class ExcelLikeSpreadSheetView extends SpreadsheetView {

    public static final SpreadsheetCellType.StringType TEXT_AREA = new SpreadsheetCellType.StringType() {
        @Override
        public SpreadsheetCellEditor createEditor(final SpreadsheetView view) {
            final TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.minHeightProperty().bind(textArea.maxHeightProperty());
            return new SpreadsheetCellEditor(view) {

                @Override
                public void startEdit(final Object value, final String format, final Object... options) {
                    if (value instanceof String || value == null) {
                        textArea.setText((String) value);
                    }
                    this.attachEnterEscapeEventHandler();
                    textArea.requestFocus();
                    textArea.selectAll();
                }

                @Override
                public String getControlValue() {
                    return textArea.getText();
                }

                @Override
                public void end() {
                    textArea.setOnKeyPressed(null);
                }

                @Override
                public TextArea getEditor() {
                    return textArea;
                }

                @Override
                public double getMaxHeight() {
                    return 1.7976931348623157E308D;
                }

                private void attachEnterEscapeEventHandler() {
                    textArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<>() {
                        @Override
                        public void handle(final KeyEvent keyEvent) {
                            if (keyEvent.getCode() == KeyCode.ENTER) {
                                if (keyEvent.isAltDown()) {
                                    textArea.replaceSelection("\n");
                                } else {
                                    endEdit(true);
                                    textArea.removeEventFilter(KeyEvent.KEY_PRESSED, this);
                                }
                                keyEvent.consume();
                            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                                endEdit(false);
                                textArea.removeEventFilter(KeyEvent.KEY_PRESSED, this);
                                keyEvent.consume();
                            } else if (keyEvent.getCode() == KeyCode.TAB) {
                                if (keyEvent.isShiftDown()) {
                                    textArea.replaceSelection("\t");
                                } else {
                                    endEdit(true);
                                    textArea.removeEventFilter(KeyEvent.KEY_PRESSED, this);
                                }
                                keyEvent.consume();
                            }
                        }
                    });
                }
            };
        }
    };

    private final Stack<GridChange> undoStack = new Stack<>();

    private final Stack<GridChange> redoStack = new Stack<>();

    private final KeyCombination undoKeypad;

    private final KeyCombination redoKeypad;

    public ExcelLikeSpreadSheetView(final GridBase grid) {
        super(grid);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, change -> {
            this.redoStack.clear();
            this.undoStack.push(change);
        });
        this.undoKeypad = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
        this.redoKeypad = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);
        this.getChildren().get(0).addEventFilter(KeyEvent.KEY_PRESSED, (keyEvent) -> {
            if (this.undoKeypad.match(keyEvent)) {
                if (this.undoStack.size() > 0) {
                    final GridChange change = this.undoStack.pop();
                    this.redoStack.push(change);
                    final SpreadsheetCell cell = this.getGrid().getRows().get(change.getRow()).get(change.getColumn());
                    final Object convertedValue = cell.getCellType().convertValue(change.getOldValue());
                    cell.setItem(convertedValue);
                    this.getSelectionModel().focus(cell.getRow(), this.getColumns().get(cell.getColumn()));
                }
            } else if (this.redoKeypad.match(keyEvent)) {
                if (this.redoStack.size() > 0) {
                    final GridChange change = this.redoStack.pop();
                    this.undoStack.push(change);
                    final SpreadsheetCell cell = this.getGrid().getRows().get(change.getRow()).get(change.getColumn());
                    final Object convertedValue = cell.getCellType().convertValue(change.getNewValue());
                    cell.setItem(convertedValue);
                    this.getSelectionModel().focus(cell.getRow(), this.getColumns().get(cell.getColumn()));
                }
            }
        });

    }

    @Override
    public void copyClipboard() {
        super.copyClipboard();
        final Object content = Clipboard.getSystemClipboard().getContent(this.getSpreadSheetDataFormat());
        if (content != null) {
            final ClipboardContent newContent = new ClipboardContent();
            newContent.put(this.getSpreadSheetDataFormat(), content);
            newContent.putString(this.toString((ArrayList<ClipboardCell>) content));
            Clipboard.getSystemClipboard().setContent(newContent);
        }
    }

    @Override
    public void pasteClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (this.isPasteOutsideApplication(clipboard)) {
            final ClipboardContent newContent = new ClipboardContent();
            newContent.put(this.getSpreadSheetDataFormat(), this.toClipboardCell(clipboard.getContent(DataFormat.PLAIN_TEXT).toString()));
            Clipboard.getSystemClipboard().setContent(newContent);
        }
        super.pasteClipboard();
    }

    protected boolean isPasteOutsideApplication(final Clipboard clipboard) {
        return clipboard.getContent(DataFormat.PLAIN_TEXT) != null && clipboard.getContent(this.getSpreadSheetDataFormat()) == null;
    }

    protected String toString(final ArrayList<ClipboardCell> content) {
        final Map<Integer, StringBuilder> copy = Maps.newLinkedHashMap();
        content.forEach(cell ->
                copy.merge(cell.getRow()
                        , new StringBuilder(cell.getValue() == null ? "" : "\"" + cell.getValue() + "\"")
                        , (summary, newVal) -> summary.append("\t").append(newVal)));
        final StringJoiner result = new StringJoiner("\r\n");
        copy.values().forEach(result::add);
        return result.toString();
    }

    protected List<ClipboardCell> toClipboardCell(final String content) {
        int rowNo = 0;
        final List<ClipboardCell> pasteContents = Lists.newArrayList();
        final CSVReader csvR = new CSVReaderBuilder(new StringReader(content)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
        for (final String[] row : csvR) {
            int columnNo = 0;
            for (final String column : row) {
                final SpreadsheetCell cell = TEXT_AREA.createCell(rowNo, columnNo, 1, 1, column);
                pasteContents.add(new ClipboardCell(rowNo, columnNo, cell));
                columnNo++;
            }
            rowNo++;
        }
        return pasteContents;
    }

    private DataFormat getSpreadSheetDataFormat() {
        DataFormat spreadSheetViewFmt;
        if ((spreadSheetViewFmt = DataFormat.lookupMimeType("SpreadsheetView")) == null) {
            spreadSheetViewFmt = new DataFormat("SpreadsheetView");
        }
        return spreadSheetViewFmt;
    }
}