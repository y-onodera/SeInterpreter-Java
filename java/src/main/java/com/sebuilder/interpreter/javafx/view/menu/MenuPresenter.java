package com.sebuilder.interpreter.javafx.view.menu;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.javafx.model.SeInterpreter;
import com.sebuilder.interpreter.javafx.view.ErrorDialog;
import com.sebuilder.interpreter.javafx.view.replay.InputView;
import com.sebuilder.interpreter.javafx.view.replay.ReplaysettingView;
import com.sebuilder.interpreter.javafx.view.replay.ScreenshotView;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.util.Optional;

public class MenuPresenter {

    @Inject
    private SeInterpreter application;
    @Inject
    private ErrorDialog errorDialog;
    @FXML
    private Pane paneSeInterpreterMenu;

    @FXML
    void handleScriptOpenFile() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final File file = this.openFileChooser("Open Resource File", "json format (*.json)", "*.json");
            if (file != null) {
                this.application.scriptReLoad(file);
            }
        });
    }

    @FXML
    void handleImportSeleniumIDEScript() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            final File file = this.openFileChooser("Import SeleniumIDE Script", "side format (*.side)", "*.side");
            if (file != null) {
                this.application.scriptReLoad(file, "SeleniumIDE");
            }
        });
    }

    @FXML
    void handleSaveSuite() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> {
            if (Strings.isNullOrEmpty(this.application.getSuite().path())) {
                this.saveSuiteToNewFile();
            } else {
                this.application.saveSuite();
            }
        });
    }

    @FXML
    void handleSaveSuiteAs() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(this::saveSuiteToNewFile);
    }

    @FXML
    void handleCreateNewSuite() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.application.reset());
    }

    @FXML
    void handleBrowserOpen() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.application.browserOpen());
    }

    @FXML
    void handleBrowserClose() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.application.browserClose());
    }

    @FXML
    void handleBrowserSetting() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> new BrowserView().open(this.paneSeInterpreterMenu.getScene().getWindow()));
    }

    @FXML
    void handleReplaySuite() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> this.application.runSuite());
    }

    @FXML
    void handleReplayScript() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> new InputView().open(this.paneSeInterpreterMenu.getScene().getWindow()));
    }

    @FXML
    void handleTakeScreenshot() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> new ScreenshotView().open(this.paneSeInterpreterMenu.getScene().getWindow()));
    }

    @FXML
    void handleReplaySetting() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> new ReplaysettingView().open(this.paneSeInterpreterMenu.getScene().getWindow()));
    }

    @FXML
    void handleOpenResult() {
        this.errorDialog.executeAndLoggingCaseWhenThrowException(() -> Desktop.getDesktop()
                .open(Context.getResultOutputDirectory()));
    }

    private File openFileChooser(final String aTitle, final String aFilterMessage, final String aFilterExtensions) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(aTitle);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(aFilterMessage, aFilterExtensions));
        fileChooser.setInitialDirectory(this.getBaseDirectory());
        final Stage stage = new Stage();
        stage.initOwner(this.paneSeInterpreterMenu.getScene().getWindow());
        return fileChooser.showOpenDialog(stage);
    }

    private void saveSuiteToNewFile() {
        final FileChooser fileSave = new FileChooser();
        fileSave.setTitle("Save Suite File");
        fileSave.getExtensionFilters().add(new FileChooser.ExtensionFilter("json format (*.json)", "*.json"));
        fileSave.setInitialDirectory(this.getBaseDirectory());
        final Stage stage = new Stage();
        stage.initOwner(this.paneSeInterpreterMenu.getScene().getWindow());
        final File file = fileSave.showSaveDialog(stage);
        if (file != null) {
            this.application.saveSuite(file);
        }
    }

    private File getBaseDirectory() {
        return Optional.ofNullable(this.application.getSuite().head().relativePath())
                .orElse(Context.getBaseDirectory());
    }

}
