package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ScreenshotView extends FXMLView {
    public void open(final Window window) {
        final Scene scene = new Scene(this.getView());
        final Stage screenshotSettingDialog = new Stage();
        screenshotSettingDialog.setScene(scene);
        screenshotSettingDialog.initOwner(window);
        screenshotSettingDialog.initModality(Modality.WINDOW_MODAL);
        screenshotSettingDialog.setResizable(false);
        screenshotSettingDialog.setTitle("screenshot config");
        screenshotSettingDialog.show();
        this.presenter().populate(null);
    }

    private ScreenshotPresenter presenter() {
        return (ScreenshotPresenter) this.getPresenter();
    }
}
