package com.sebuilder.interpreter.javafx.view.replay;

import com.airhacks.afterburner.views.FXMLView;
import com.sebuilder.interpreter.InputData;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class VariableView extends FXMLView {

    public VariableView(final Builder builder) {
        super();
        final Scene scene = new Scene(this.getView());
        final Stage stage = new Stage();
        this.presenter().setOnclick(builder.getOnclick());
        this.presenter().populate(builder.getTarget());
        stage.initOwner(builder.getWindow());
        stage.setResizable(false);
        stage.setTitle(builder.getTitle());
        stage.setScene(scene);
        stage.show();
    }

    public static Builder builder() {
        return new Builder();
    }

    private VariablePresenter presenter() {
        return (VariablePresenter) this.getPresenter();
    }

    public static class Builder {
        private String title;
        private Consumer<InputData> onclick;

        private InputData target;

        private Window window;

        public String getTitle() {
            return this.title;
        }

        public Builder setTitle(final String title) {
            this.title = title;
            return this;
        }

        public Consumer<InputData> getOnclick() {
            return this.onclick;
        }

        public Builder setOnclick(final Consumer<InputData> onclick) {
            this.onclick = onclick;
            return this;
        }

        public InputData getTarget() {
            return this.target;
        }

        public Builder setTarget(final InputData target) {
            this.target = target;
            return this;
        }

        public Window getWindow() {
            return this.window;
        }

        public Builder setWindow(final Window window) {
            this.window = window;
            return this;
        }

        public VariableView build() {
            return new VariableView(this);
        }
    }
}
