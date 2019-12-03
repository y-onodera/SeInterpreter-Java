package com.sebuilder.interpreter.javafx.view.menu;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DatasourceView extends FXMLView {

    public void open(Window window) {
        Scene scene = new Scene(this.getView());
        Stage scriptSettingDialog = new Stage();
        scriptSettingDialog.setScene(scene);
        scriptSettingDialog.initOwner(window);
        scriptSettingDialog.initModality(Modality.WINDOW_MODAL);
        scriptSettingDialog.setResizable(false);
        scriptSettingDialog.setTitle("edit script setting");
        scriptSettingDialog.show();
    }

}
