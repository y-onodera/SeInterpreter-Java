package com.sebuilder.interpreter.javafx.view;

import javafx.scene.control.Alert;

public class SuccessDialog {

    public static void show(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
