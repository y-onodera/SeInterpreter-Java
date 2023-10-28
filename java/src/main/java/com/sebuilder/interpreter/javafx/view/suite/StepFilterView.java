package com.sebuilder.interpreter.javafx.view.suite;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.Pointcut;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class StepFilterView extends FXMLView {

    private final Pointcut defaultValue;

    public StepFilterView(final Pointcut defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void open(final Window window, final Pointcut init, final Consumer<Pointcut> onCommit) {
        final Scene scene = new Scene(this.getView());
        final Stage dialog = new Stage();
        dialog.setScene(scene);
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("edit filter");
        dialog.setResizable(false);
        this.getPresenter().populate(dialog, init, onCommit);
        this.getPresenter().setDefaultValue(this.defaultValue);
        dialog.show();
    }

    @Override
    public StepFilterPresenter getPresenter() {
        return (StepFilterPresenter) super.getPresenter();
    }
}
