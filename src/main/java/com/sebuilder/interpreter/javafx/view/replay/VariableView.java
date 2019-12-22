package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.InputData;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class VariableView extends FXMLView {

    public void open(InputData var, Window window) {
        this.presenter().open(var);
        Stage stage = new Stage();
        stage.initOwner(window);
        stage.setResizable(false);
        Scene scene = new Scene(this.getView());
        stage.setTitle("runtime variable");
        stage.setScene(scene);
        stage.show();
    }

    public void onClick(Consumer<InputData> handler){
        this.presenter().setOnclick(handler);
    }

    private VariablePresenter presenter() {
        return (VariablePresenter) this.getPresenter();
    }

}
