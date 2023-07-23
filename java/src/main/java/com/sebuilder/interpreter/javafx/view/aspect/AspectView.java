package com.sebuilder.interpreter.javafx.view.aspect;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.TestCase;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AspectView extends FXMLView {
    public void open(final Window window, final TestCase target) {
        final Scene scene = new Scene(this.getView());
        final Stage scriptSettingDialog = new Stage();
        scriptSettingDialog.setScene(scene);
        scriptSettingDialog.initOwner(window);
        scriptSettingDialog.initModality(Modality.WINDOW_MODAL);
        scriptSettingDialog.setResizable(true);
        scriptSettingDialog.setTitle("aspect");
        this.getPresenter().setRootProperty(target);
        scriptSettingDialog.show();
    }

    @Override
    public AspectPresenter getPresenter() {
        return (AspectPresenter) super.getPresenter();
    }
}
