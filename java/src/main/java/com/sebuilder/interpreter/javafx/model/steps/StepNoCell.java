package com.sebuilder.interpreter.javafx.model.steps;

import javafx.scene.control.TableCell;

public class StepNoCell extends TableCell<StepDefine, StepNo> {
    @Override
    protected void updateItem(final StepNo item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            this.setText(item.no().toString());
            this.setGraphic(item.breakPoint());
        }
    }
}
