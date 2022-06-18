package com.sebuilder.interpreter.javafx.control;

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
    public TreeCell<T> call(TreeView<T> tree) {
        TreeCell<T> cell = new TreeCell<>() {
            @Override
            protected void updateItem(T t, boolean b) {
                super.updateItem(t, b);
                updateItemCallback(this, t, b);
            }
        };
        cell.setOnDragDetected(event -> dragDetected(event, cell, tree));
        cell.setOnDragOver(event -> dragOver(event, cell, tree));
        cell.setOnDragDropped(event -> dragDropped(event, cell, tree));
        cell.setOnDragDone(event -> dragDone(event, cell, tree));
        return cell;
    }

    protected abstract void updateItemCallback(TreeCell<T> treeCell, T t, boolean b);

    public TreeItem<T> getDraggedItem() {
        return this.draggedItem;
    }

    protected void dragDetected(MouseEvent event, TreeCell<T> treeCell, TreeView<T> treeView) {
        draggedItem = treeCell.getTreeItem();
        // root can't be dragged
        if (this.draggedItem.getParent() == null) {
            return;
        }
        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(Constant.SERIALIZED_MIME_TYPE, draggedItem.getValue());
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    protected void dragOver(DragEvent event, TreeCell<T> treeCell, TreeView<T> treeView) {
        if (!event.getDragboard().hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            return;
        }
        TreeItem<T> thisItem = treeCell.getTreeItem();
        // can't drop on itself
        if (this.draggedItem == null || thisItem == null || thisItem == this.draggedItem) {
            return;
        }
        // ignore if this is the root
        if (this.draggedItem.getParent() == null) {
            clearDropLocation();
            return;
        }
        event.acceptTransferModes(TransferMode.MOVE);
        if (!Objects.equals(this.dropZone, treeCell)) {
            clearDropLocation();
            this.dropZone = treeCell;
            this.dropZone.setStyle(Constant.DROP_HINT_STYLE);
        }
    }

    protected void dragDropped(DragEvent event, TreeCell<T> treeCell, TreeView<T> treeView) {
        Dragboard db = event.getDragboard();
        if (!db.hasContent(Constant.SERIALIZED_MIME_TYPE)) {
            return;
        }
        TreeItem<T> thisItem = treeCell.getTreeItem();
        TreeItem<T> draggedItemParent = this.draggedItem.getParent();
        // remove from previous location
        removeDragItemFromPreviousParent(draggedItemParent);
        // dropping on parent node makes it the first child
        if (Objects.equals(draggedItemParent, thisItem) || thisItem.getChildren().size() > 0) {
            addDropItemToNewParent(thisItem, 0);
        } else {
            // add to new location
            int indexInParent = thisItem.getParent().getChildren().indexOf(thisItem);
            addDropItemToNewParent(thisItem.getParent(), indexInParent + 1);
        }
        event.setDropCompleted(false);
    }

    protected void addDropItemToNewParent(TreeItem<T> droppedItemParent, int i) {
        droppedItemParent.getChildren().add(i, this.draggedItem);
    }

    protected void removeDragItemFromPreviousParent(TreeItem<T> draggedItemParent) {
        draggedItemParent.getChildren().remove(this.draggedItem);
    }

    protected void dragDone(DragEvent event, TreeCell<T> treeCell, TreeView<T> treeView) {
        this.clearDropLocation();
    }

    protected void clearDropLocation() {
        if (this.dropZone != null) {
            this.dropZone.setStyle("");
        }
    }
}