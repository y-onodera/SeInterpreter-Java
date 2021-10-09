package com.sebuilder.interpreter.javafx.control;

import com.sebuilder.interpreter.javafx.Constant;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.*;
import javafx.util.Callback;

import java.util.Objects;

public abstract class DragAndDropTableViewRowFactory<T> implements Callback<TableView<T>, TableRow<T>> {
    private TableRow<T> dropZone;

    @Override
    public TableRow<T> call(TableView<T> tableView) {
        TableRow<T> row = new TableRow<>() {
            @Override
            protected void updateItem(T scriptBody, boolean b) {
                super.updateItem(scriptBody, b);
                updateItemCallback(this,scriptBody, b);
            }

        };
        row.setOnDragDetected(event -> this.dragDetected(event, row, tableView));
        row.setOnDragOver(event -> this.dragOver(event, row, tableView));
        row.setOnDragDropped(event -> this.dragAndDrop(event, row, tableView));
        row.setOnDragDone(event -> this.clearDropLocation());
        return row;
    }

    protected void updateItemCallback(TableRow<T> tableRow, T scriptBody, boolean b) {
        // default non implement
    }

    protected void dragDetected(MouseEvent event, TableRow<T> row, TableView<T> tableView) {
        if (!row.isEmpty()) {
            Integer index = row.getIndex();
            Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(row.snapshot(null, null));
            ClipboardContent cc = new ClipboardContent();
            cc.put(Constant.SERIALIZED_MIME_TYPE, index);
            db.setContent(cc);
            event.consume();
        }
    }

    protected void dragOver(DragEvent event, TableRow<T> row, TableView<T> tableView) {
        Dragboard db = event.getDragboard();
        if (db.hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            if (row.getIndex() != (Integer) db.getContent(Constant.SERIALIZED_MIME_TYPE)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                if (!Objects.equals(this.dropZone, row)) {
                    clearDropLocation();
                    this.dropZone = row;
                    this.dropZone.setStyle(Constant.DROP_HINT_STYLE);
                }
                event.consume();
            }
        }
    }

    protected void dragAndDrop(DragEvent event, TableRow<T> row, TableView<T> tableView) {
        Dragboard db = event.getDragboard();
        if (db.hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            int draggedIndex = (Integer) db.getContent(Constant.SERIALIZED_MIME_TYPE);
            int dropIndex;
            if (row.isEmpty()) {
                dropIndex = tableView.getItems().size() - 1;
            } else {
                dropIndex = row.getIndex();
            }
            move(draggedIndex, dropIndex);
            event.setDropCompleted(false);
            event.consume();
        }
    }

    protected abstract void move(int draggedIndex, int dropIndex);

    protected void clearDropLocation() {
        if (this.dropZone != null) {
            this.dropZone.setStyle("");
        }
    }
}
