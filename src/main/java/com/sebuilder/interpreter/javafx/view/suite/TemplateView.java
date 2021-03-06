package com.sebuilder.interpreter.javafx.view.suite;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class TemplateView extends FXMLView {

    public TemplateView(Window window) {
        Scene scene = new Scene(this.getView());
        Stage exportSettingDialog = new Stage();
        exportSettingDialog.setScene(scene);
        exportSettingDialog.initOwner(window);
        exportSettingDialog.initModality(Modality.WINDOW_MODAL);
        exportSettingDialog.setResizable(false);
        exportSettingDialog.setTitle("edit export setting");
        exportSettingDialog.show();
    }
}
