package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.javafx.model.SeInterpreterRunTask;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ReplayView extends FXMLView {

    public void open(final Window window, final SeInterpreterRunTask task) {
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

    protected ReplayPresenter presenter() {
        return (ReplayPresenter) this.getPresenter();
    }

}
