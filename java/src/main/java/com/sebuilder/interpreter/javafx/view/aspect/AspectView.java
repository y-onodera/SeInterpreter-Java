package com.sebuilder.interpreter.javafx.view.aspect;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.Aspect;
import com.sebuilder.interpreter.TestCase;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;

import java.util.function.Consumer;

public class AspectView extends FXMLView {

    private AspectView(final Builder builder) {
        final Scene scene = new Scene(this.getView());
        final Stage scriptSettingDialog = new Stage();
        scriptSettingDialog.setScene(scene);
        scriptSettingDialog.initOwner(builder.getWindow());
        scriptSettingDialog.initModality(Modality.WINDOW_MODAL);
        scriptSettingDialog.setResizable(true);
        scriptSettingDialog.setTitle("aspect");
        this.getPresenter().setRootProperty(builder.getTarget());
        this.getPresenter().setOnClickCommit(builder.getOnclick());
        scriptSettingDialog.show();

    }

    public void open(final Window window, final TestCase target) {
    }

    @Override
    public AspectPresenter getPresenter() {
        return (AspectPresenter) super.getPresenter();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Consumer<Aspect> onclick;

        private Pair<String, Aspect> target;

        private Window window;

        public Consumer<Aspect> getOnclick() {
            return this.onclick;
        }

        public Builder setOnclick(final Consumer<Aspect> onclick) {
            this.onclick = onclick;
            return this;
        }

        public Pair<String, Aspect> getTarget() {
            return this.target;
        }

        public Builder setTarget(final Pair<String, Aspect> target) {
            this.target = target;
            return this;
        }

        public Builder setTarget(final TestCase target) {
            this.target = new Pair<>(target.name(), target.aspect());
            return this;
        }

        public Window getWindow() {
            return this.window;
        }

        public Builder setWindow(final Window window) {
            this.window = window;
            return this;
        }

        public AspectView build() {
            return new AspectView(this);
        }
    }

}
