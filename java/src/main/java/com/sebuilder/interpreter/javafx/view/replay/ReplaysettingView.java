package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ReplaysettingView extends FXMLView {

    public void open(final Window window) {
        final Scene scene = new Scene(this.getView());
        final Stage scriptSettingDialog = new Stage();
        scriptSettingDialog.setScene(scene);
        scriptSettingDialog.initOwner(window);
        scriptSettingDialog.initModality(Modality.WINDOW_MODAL);
        scriptSettingDialog.setResizable(false);
        scriptSettingDialog.setTitle("edit replay setting");
        scriptSettingDialog.show();
    }

}
