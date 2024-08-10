package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.javafx.model.ReplayOption;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class InputView extends FXMLView {

    public void open(final Window window) {
        final Scene scene = new Scene(this.getView());
        final Stage scriptSettingDialog = new Stage();
        scriptSettingDialog.setScene(scene);
        scriptSettingDialog.initOwner(window);
        scriptSettingDialog.initModality(Modality.WINDOW_MODAL);
        scriptSettingDialog.setResizable(false);
        scriptSettingDialog.setTitle("input config");
        scriptSettingDialog.show();
    }

    public void setOnclickReplayStart(final Consumer<ReplayOption> onClick) {
        ((InputPresenter) this.getPresenter()).setOnclickReplayStart(onClick);
    }
}
