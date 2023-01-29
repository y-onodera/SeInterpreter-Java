package com.sebuilder.interpreter.javafx.view.script;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class BreakPointView extends FXMLView {

    public void open(final Window window, final int stepIndex) {
        final Scene scene = new Scene(this.getView());
        final Stage dialog = new Stage();
        this.presenter().populate(dialog, stepIndex);
        dialog.setScene(scene);
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("add breakpoint");
        dialog.setResizable(true);
        dialog.show();
    }

    protected BreakPointPresenter presenter() {
        return (BreakPointPresenter) this.getPresenter();
    }

}
