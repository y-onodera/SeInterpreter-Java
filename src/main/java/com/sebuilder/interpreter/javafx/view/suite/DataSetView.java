package com.sebuilder.interpreter.javafx.view.suite;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.TestCase;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DataSetView extends FXMLView {

    public void showDataSet(TestCase currentCase, Window parentWindow) {
        DataSetPresenter.class.cast(this.getPresenter()).showDataSet(currentCase);
        Stage stage = new Stage();
        stage.initOwner(parentWindow);
        Scene scene = new Scene(this.getView());
        stage.setTitle(currentCase.loadDataFrom());
        stage.setScene(scene);
        stage.show();
    }

}
