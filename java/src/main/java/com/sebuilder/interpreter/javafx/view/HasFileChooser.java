package com.sebuilder.interpreter.javafx.view;

import com.sebuilder.interpreter.Context;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public interface HasFileChooser {

    default File openDialog(final String aTitle, final String aFilterMessage, final String aFilterExtensions) {
        return this.createFileChooser(aTitle, aFilterMessage, aFilterExtensions)
                .showOpenDialog(this.currentWindow());
    }

    default File saveDialog(final String aTitle, final String aFilterMessage, final String aFilterExtensions) {
        return this.createFileChooser(aTitle, aFilterMessage, aFilterExtensions)
                .showSaveDialog(this.currentWindow());
    }

    default FileChooser createFileChooser(final String aTitle, final String aFilterMessage, final String aFilterExtensions) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(aTitle);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(aFilterMessage, aFilterExtensions));
        fileChooser.setInitialDirectory(this.getBaseDirectory());
        return fileChooser;
    }

    default File getBaseDirectory() {
        return Context.getBaseDirectory();
    }

    Window currentWindow();


}
