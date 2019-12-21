package com.sebuilder.interpreter.javafx.view.data;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.DataSourceLoader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DataSetView extends FXMLView {

    public void showDataSet(DataSourceLoader resource, Window parentWindow) {
        this.presenter().showDataSet(resource);
        Stage stage = new Stage();
        stage.initOwner(parentWindow);
        Scene scene = new Scene(this.getView());
        stage.setTitle(resource.name());
        stage.setScene(scene);
        stage.show();
    }

    public void onClick(EventHandler<ActionEvent> handler){
        this.presenter().setOnclick(handler);
    }

    private DataSetPresenter presenter() {
        return (DataSetPresenter) this.getPresenter();
    }

}
