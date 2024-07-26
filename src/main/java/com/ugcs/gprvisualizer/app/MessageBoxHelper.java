package com.ugcs.gprvisualizer.app;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

public class MessageBoxHelper {

	public static void showError(String header, String msg) {
		
		System.out.println("MessageBoxHelper.showError " + header + " " + msg);
		
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(header);
			alert.setContentText(msg);
			alert.showAndWait();//.ifPresent(rs -> {

			//});
		});
	}	
	
}
