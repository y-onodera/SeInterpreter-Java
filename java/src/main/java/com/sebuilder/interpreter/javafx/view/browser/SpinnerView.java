package com.sebuilder.interpreter.javafx.view.browser;

import com.airhacks.afterburner.views.FXMLView;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class SpinnerView extends FXMLView {

    public void open(final Window window, final Task<?> task) {
        final Scene scene = new Scene(this.getView());
        final Stage runProgressDialog = new Stage();
        this.presenter().populate(task);
        runProgressDialog.setScene(scene);
        runProgressDialog.initOwner(window);
        runProgressDialog.initModality(Modality.WINDOW_MODAL);
        runProgressDialog.setTitle("run progress");
        runProgressDialog.setResizable(false);
        runProgressDialog.show();
    }

    protected SpinnerPresenter presenter() {
        return (SpinnerPresenter) this.getPresenter();
    }
}
