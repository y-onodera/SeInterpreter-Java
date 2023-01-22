package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.InputData;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class VariableView extends FXMLView {

    public void open(final InputData var, final Window window) {
        final Scene scene = new Scene(this.getView());
        final Stage stage = new Stage();
        this.presenter().populate(var);
        stage.initOwner(window);
        stage.setResizable(false);
        stage.setTitle("runtime variable");
        stage.setScene(scene);
        stage.show();
    }

    public void onClick(final Consumer<InputData> handler) {
        this.presenter().setOnclick(handler);
    }

    private VariablePresenter presenter() {
        return (VariablePresenter) this.getPresenter();
    }

}
