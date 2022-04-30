package com.sebuilder.interpreter.javafx.control;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import org.controlsfx.control.spreadsheet.*;

import java.io.StringReader;
import java.util.*;

public class ExcelLikeSpreadSheetView extends SpreadsheetView {

    public static final SpreadsheetCellType.StringType TEXT_AREA = new SpreadsheetCellType.StringType() {
        @Override
        public SpreadsheetCellEditor createEditor(SpreadsheetView view) {
            TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.minHeightProperty().bind(textArea.maxHeightProperty());
            return new SpreadsheetCellEditor(view) {

                public void startEdit(Object value, String format, Object... options) {
                    if (value instanceof String || value == null) {
                        textArea.setText((String) value);
                    }
                    this.attachEnterEscapeEventHandler();
                    textArea.requestFocus();
                    textArea.selectAll();
                }

                public String getControlValue() {
                    return textArea.getText();
                }

                public void end() {
                    textArea.setOnKeyPressed(null);
                }

                public TextArea getEditor() {
                    return textArea;
                }

                public double getMaxHeight() {
                    return 1.7976931348623157E308D;
                }

                private void attachEnterEscapeEventHandler() {
                    textArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                        public void handle(KeyEvent keyEvent) {
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

    public ExcelLikeSpreadSheetView(GridBase grid) {
        super(grid);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, change -> {
            redoStack.clear();
            undoStack.push(change);
        });
        this.undoKeypad = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
        this.redoKeypad = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);
        this.getChildren().get(0).addEventFilter(KeyEvent.KEY_PRESSED, (keyEvent) -> {
            if (undoKeypad.match(keyEvent)) {
                if (undoStack.size() > 0) {
                    GridChange change = undoStack.pop();
                    redoStack.push(change);
                    SpreadsheetCell cell = (SpreadsheetCell) ((ObservableList) this.getGrid().getRows().get(change.getRow())).get(change.getColumn());
                    Object convertedValue = cell.getCellType().convertValue(change.getOldValue());
                    cell.setItem(convertedValue);
                    this.getSelectionModel().focus(cell.getRow(), getColumns().get(cell.getColumn()));
                }
            } else if (redoKeypad.match(keyEvent)) {
                if (redoStack.size() > 0) {
                    GridChange change = redoStack.pop();
                    undoStack.push(change);
                    SpreadsheetCell cell = (SpreadsheetCell) ((ObservableList) this.getGrid().getRows().get(change.getRow())).get(change.getColumn());
                    Object convertedValue = cell.getCellType().convertValue(change.getNewValue());
                    cell.setItem(convertedValue);
                    this.getSelectionModel().focus(cell.getRow(), getColumns().get(cell.getColumn()));
                }
            }
        });

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
        copy.values().forEach(result::add);
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
        DataFormat spreadSheetViewFmt;
        if ((spreadSheetViewFmt = DataFormat.lookupMimeType("SpreadsheetView")) == null) {
            spreadSheetViewFmt = new DataFormat("SpreadsheetView");
        }
        return spreadSheetViewFmt;
    }
}