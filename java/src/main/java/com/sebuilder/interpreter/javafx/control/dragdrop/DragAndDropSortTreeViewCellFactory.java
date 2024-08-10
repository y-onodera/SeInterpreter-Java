package com.sebuilder.interpreter.javafx.control.dragdrop;

import com.sebuilder.interpreter.javafx.Constant;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;

import java.util.Objects;

public abstract class DragAndDropSortTreeViewCellFactory<T> implements javafx.util.Callback<javafx.scene.control.TreeView<T>, javafx.scene.control.TreeCell<T>> {
    private TreeCell<T> dropZone;
    private TreeItem<T> draggedItem;

    @Override
    public TreeCell<T> call(final TreeView<T> tree) {
        final TreeCell<T> cell = new TreeCell<>() {
            @Override
            protected void updateItem(final T t, final boolean b) {
                super.updateItem(t, b);
                DragAndDropSortTreeViewCellFactory.this.updateItemCallback(this, t, b);
            }
        };
        cell.setOnDragDetected(event -> this.dragDetected(event, cell, tree));
        cell.setOnDragOver(event -> this.dragOver(event, cell, tree));
        cell.setOnDragDropped(event -> this.dragDropped(event, cell, tree));
        cell.setOnDragDone(event -> this.dragDone(event, cell, tree));
        return cell;
    }

    protected abstract void updateItemCallback(TreeCell<T> treeCell, T t, boolean b);

    public TreeItem<T> getDraggedItem() {
        return this.draggedItem;
    }

    protected void dragDetected(final MouseEvent event, final TreeCell<T> treeCell, final TreeView<T> treeView) {
        this.draggedItem = treeCell.getTreeItem();
        // root can't be dragged
        if (this.draggedItem.getParent() == null) {
            return;
        }
        final Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);
        final ClipboardContent content = new ClipboardContent();
        content.put(Constant.SERIALIZED_MIME_TYPE, this.draggedItem.getValue());
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    protected void dragOver(final DragEvent event, final TreeCell<T> treeCell, final TreeView<T> treeView) {
        if (!event.getDragboard().hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            return;
        }
        final TreeItem<T> thisItem = treeCell.getTreeItem();
        // can't drop on itself
        if (this.draggedItem == null || thisItem == null || thisItem == this.draggedItem) {
            return;
        }
        // ignore if this is the root
        if (this.draggedItem.getParent() == null) {
            this.clearDropLocation();
            return;
        }
        event.acceptTransferModes(TransferMode.MOVE);
        if (!Objects.equals(this.dropZone, treeCell)) {
            this.clearDropLocation();
            this.dropZone = treeCell;
            this.dropZone.setStyle(Constant.DROP_HINT_STYLE);
        }
    }

    protected void dragDropped(final DragEvent event, final TreeCell<T> treeCell, final TreeView<T> treeView) {
        final Dragboard db = event.getDragboard();
        if (!db.hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            return;
        }
        final TreeItem<T> thisItem = treeCell.getTreeItem();
        final TreeItem<T> draggedItemParent = this.draggedItem.getParent();
        // remove from previous location
        this.removeDragItemFromPreviousParent(draggedItemParent);
        // dropping on parent node makes it the first child
        if (Objects.equals(draggedItemParent, thisItem) || thisItem.getChildren().size() > 0) {
            this.addDropItemToNewParent(thisItem, 0);
        } else {
            // add to new location
            final int indexInParent = thisItem.getParent().getChildren().indexOf(thisItem);
            this.addDropItemToNewParent(thisItem.getParent(), indexInParent + 1);
        }
        event.setDropCompleted(false);
    }

    protected void addDropItemToNewParent(final TreeItem<T> droppedItemParent, final int i) {
        droppedItemParent.getChildren().add(i, this.draggedItem);
    }

    protected void removeDragItemFromPreviousParent(final TreeItem<T> draggedItemParent) {
        draggedItemParent.getChildren().remove(this.draggedItem);
    }

    protected void dragDone(final DragEvent event, final TreeCell<T> treeCell, final TreeView<T> treeView) {
        this.clearDropLocation();
    }

    protected void clearDropLocation() {
        if (this.dropZone != null) {
            this.dropZone.setStyle("");
        }
    }
}
