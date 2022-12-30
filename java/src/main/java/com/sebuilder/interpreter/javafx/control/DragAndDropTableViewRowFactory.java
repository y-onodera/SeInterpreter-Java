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
    public TableRow<T> call(final TableView<T> tableView) {
        final TableRow<T> row = new TableRow<>() {
            @Override
            protected void updateItem(final T scriptBody, final boolean b) {
                super.updateItem(scriptBody, b);
                DragAndDropTableViewRowFactory.this.updateItemCallback(this, scriptBody, b);
            }

        };
        row.setOnDragDetected(event -> this.dragDetected(event, row, tableView));
        row.setOnDragOver(event -> this.dragOver(event, row, tableView));
        row.setOnDragDropped(event -> this.dragAndDrop(event, row, tableView));
        row.setOnDragDone(event -> this.clearDropLocation());
        return row;
    }

    protected void updateItemCallback(final TableRow<T> tableRow, final T scriptBody, final boolean b) {
        // default non implement
    }

    protected void dragDetected(final MouseEvent event, final TableRow<T> row, final TableView<T> tableView) {
        if (!row.isEmpty()) {
            final Integer index = row.getIndex();
            final Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(row.snapshot(null, null));
            final ClipboardContent cc = new ClipboardContent();
            cc.put(Constant.SERIALIZED_MIME_TYPE, index);
            db.setContent(cc);
            event.consume();
        }
    }

    protected void dragOver(final DragEvent event, final TableRow<T> row, final TableView<T> tableView) {
        final Dragboard db = event.getDragboard();
        if (db.hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            if (row.getIndex() != (Integer) db.getContent(Constant.SERIALIZED_MIME_TYPE)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                if (!Objects.equals(this.dropZone, row)) {
                    this.clearDropLocation();
                    this.dropZone = row;
                    this.dropZone.setStyle(Constant.DROP_HINT_STYLE);
                }
                event.consume();
            }
        }
    }

    protected void dragAndDrop(final DragEvent event, final TableRow<T> row, final TableView<T> tableView) {
        final Dragboard db = event.getDragboard();
        if (db.hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            final int draggedIndex = (Integer) db.getContent(Constant.SERIALIZED_MIME_TYPE);
            final int dropIndex;
            if (row.isEmpty()) {
                dropIndex = tableView.getItems().size() - 1;
            } else {
                dropIndex = row.getIndex();
            }
            this.move(draggedIndex, dropIndex);
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
