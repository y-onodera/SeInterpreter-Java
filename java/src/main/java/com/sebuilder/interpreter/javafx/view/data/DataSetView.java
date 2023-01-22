package com.sebuilder.interpreter.javafx.view.data;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.DataSourceLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class DataSetView extends FXMLView {

    public void open(final DataSourceLoader resource, final Window parentWindow) throws IOException {
        final Scene scene = new Scene(this.getView());
        final Stage stage = new Stage();
        this.presenter().populate(resource);
        stage.initOwner(parentWindow);
        stage.setTitle(resource.name());
        stage.setScene(scene);
        stage.show();
    }

    public void onClick(final EventHandler<ActionEvent> handler) {
        this.presenter().setOnclick(handler);
    }

    private DataSetPresenter presenter() {
        return (DataSetPresenter) this.getPresenter();
    }

}
