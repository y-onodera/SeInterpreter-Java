package com.sebuilder.interpreter.javafx.view.menu;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class BrowserView extends FXMLView {

    public void open(final Window window) {
        final Scene scene = new Scene(this.getView());
        final Stage browserSettingDialog = new Stage();
        browserSettingDialog.setScene(scene);
        browserSettingDialog.initOwner(window);
        browserSettingDialog.initModality(Modality.WINDOW_MODAL);
        browserSettingDialog.setResizable(false);
        browserSettingDialog.setTitle("edit browser setting");
        browserSettingDialog.show();
    }

}
