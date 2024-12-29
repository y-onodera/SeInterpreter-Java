package com.sebuilder.interpreter.javafx.view.step;

import com.airhacks.afterburner.views.FXMLView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public class HttpHeaderView extends FXMLView {

    public HttpHeaderView(final Builder builder) {
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

    private HttpHeaderPresenter presenter() {
        return (HttpHeaderPresenter) this.getPresenter();
    }

    public static class Builder {
        private String title;
        private Consumer<HttpHeaders> onclick;

        private HttpHeaders target;

        private Window window;

        public String getTitle() {
            return this.title;
        }

        public Builder setTitle(final String title) {
            this.title = title;
            return this;
        }

        public Consumer<HttpHeaders> getOnclick() {
            return this.onclick;
        }

        public Builder setOnclick(final Consumer<HttpHeaders> onclick) {
            this.onclick = onclick;
            return this;
        }

        public HttpHeaders getTarget() {
            return this.target;
        }

        public Builder setTarget(final HttpHeaders target) {
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

        public HttpHeaderView build() {
            return new HttpHeaderView(this);
        }
    }
}
