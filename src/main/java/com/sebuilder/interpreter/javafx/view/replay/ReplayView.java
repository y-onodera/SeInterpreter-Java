package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ReplayView extends FXMLView {

    public void open(Window window, Task task) {
        Scene scene = new Scene(this.getView());
        Stage runProgressDialog = new Stage();
        runProgressDialog.setScene(scene);
        runProgressDialog.initOwner(window);
        runProgressDialog.initModality(Modality.WINDOW_MODAL);
        runProgressDialog.setTitle("run progress");
        ReplayPresenter.class.cast(this.getPresenter()).bind(task);
        runProgressDialog.setResizable(false);
        runProgressDialog.show();
    }

}
