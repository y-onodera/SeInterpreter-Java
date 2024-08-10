package com.sebuilder.interpreter.javafx.view.step;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.Step;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class StepView extends FXMLView {

    private final Predicate<String> stepTypeFilter;

    private final Predicate<String> textParamFilter;

    public StepView() {
        this(s -> true, s -> true);
    }

    public StepView(final Predicate<String> stepTypeFilter, final Predicate<String> textParamFilter) {
        this.stepTypeFilter = stepTypeFilter;
        this.textParamFilter = textParamFilter;
    }

    public void open(final Window window, final Consumer<Step> applyStep) {
        final Scene scene = new Scene(this.getView());
        final Stage dialog = new Stage();
        this.presenter().populate(dialog, this.stepTypeFilter, this.textParamFilter, applyStep);
        dialog.setScene(scene);
        dialog.initOwner(window);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("edit step");
        dialog.setResizable(false);
        dialog.show();
    }

    public void refresh(final Step step) {
        this.presenter().refreshView(step);
    }

    protected StepPresenter presenter() {
        return (StepPresenter) this.getPresenter();
    }


}
