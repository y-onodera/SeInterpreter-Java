package com.sebuilder.interpreter.javafx.view.filter;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.Pointcut;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class FilterView extends FXMLView {
    public void open(final Window window, final Consumer<Pointcut> applyAction) {
        final Scene scene = new Scene(this.getView());
        final Stage dialog = new Stage();
        dialog.setScene(scene);
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("edit filter");
        dialog.setResizable(false);
        this.presenter().setApplyAction(dialog, applyAction);
        dialog.show();
    }

    public void refresh(final Pointcut pointcut) {
        this.presenter().refreshView(pointcut);
    }

    protected FilterPresenter presenter() {
        return (FilterPresenter) this.getPresenter();
    }
}
