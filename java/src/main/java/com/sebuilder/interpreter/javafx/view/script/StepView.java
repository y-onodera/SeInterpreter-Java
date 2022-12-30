package com.sebuilder.interpreter.javafx.view.script;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.Step;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class StepView extends FXMLView {


    public StepView(final Window window, final int no, final Action action) {
        final Scene scene = new Scene(this.getView());
        final Stage dialog = new Stage();
        dialog.setScene(scene);
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("edit step");
        this.presenter().init(dialog, no, action);
        dialog.setResizable(true);
        dialog.show();
    }

    public void refresh(final Step step) {
        this.presenter().refreshView(step);
    }

    protected StepPresenter presenter() {
        return (StepPresenter) this.getPresenter();
    }

    public enum Action {
        INSERT, ADD, EDIT
    }

}
