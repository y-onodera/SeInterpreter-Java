package com.sebuilder.interpreter.javafx.application;

import com.airhacks.afterburner.injection.Injector;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.SuccessDialog;
import com.sebuilder.interpreter.javafx.view.main.MainView;
import com.sebuilder.interpreter.javafx.view.replay.ReplayView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class SeInterpreterApplication extends Application {

    private SeInterpreter seInterpreter;

    private MainView mainView;

    private ErrorDialog errorDialog;

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        System.setProperty("log4j.configurationFile", "log4j2-gui.xml");
        final Parameters parameters = this.getParameters();
        this.seInterpreter = new SeInterpreter(parameters.getRaw()
                , (final File target, final String content) ->
                this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
                    if (!target.exists()) {
                        Files.createFile(target.toPath());
                    }
                    Files.writeString(target.toPath(), content, StandardCharsets.UTF_8);
                    SuccessDialog.show("save succeed:" + target.getAbsolutePath());
                })
                , (task) -> new ReplayView().open(this.mainView.getMainWindow(), task)
                , (action) -> this.errorDialog.executeAndLoggingCaseWhenThrowException(action));
        this.errorDialog = new ErrorDialog(this.seInterpreter.runner().getLog());
        Injector.setModelOrService(SeInterpreter.class, this.seInterpreter);
        Injector.setModelOrService(ErrorDialog.class, this.errorDialog);
        final List<String> unnamed = parameters.getUnnamed();
        if (!unnamed.isEmpty()) {
            this.seInterpreter.resetSuite(new File(unnamed.getFirst()));
        } else {
            this.seInterpreter.reset();
        }
        this.seInterpreter
                .reloadScreenshotTemplate(Optional.ofNullable(parameters.getNamed().get("takeScreenshotTemplate"))
                        .map(File::new)
                        .orElse(null));
        this.mainView = new MainView();
        this.mainView.open(stage, this.seInterpreter::changeScriptViewType);
    }

    @Override
    public void stop() throws Exception {
        this.seInterpreter.runner().close();
        super.stop();
    }

}
