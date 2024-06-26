package com.sebuilder.interpreter.javafx.view.main;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.javafx.model.ViewType;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class MainView extends FXMLView {
    private Window window;

    public void open(final Stage stage, final Consumer<ViewType> viewSelectedHandler) {
        this.getPresenter().setHandleViewSelected(viewSelectedHandler);
        final Scene scene = new Scene(this.getView());
        stage.setTitle("SeInterpreter");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
        this.window = scene.getWindow();
    }

    public Window getMainWindow() {
        return this.window;
    }

    @Override
    public MainPresenter getPresenter() {
        return (MainPresenter) super.getPresenter();
    }
}
