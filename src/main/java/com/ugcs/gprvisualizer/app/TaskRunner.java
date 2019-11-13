package com.ugcs.gprvisualizer.app;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TaskRunner implements ProgressListener {
	private Stage primaryStage;
	private Stage dialog = new Stage();
	private ProgressTask task;
	private Text text = new Text("This is a Dialog");
	
	public TaskRunner(Stage primaryStage, ProgressTask task) {
		this.primaryStage = primaryStage;
		this.task = task;
	}
	
	public void start() {
		
		new Thread() {
			public void run() {
				
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						showPopup();
					}
				});
				
				
				task.run(TaskRunner.this);
				
				Platform.runLater(new Runnable(){
					@Override
					public void run() {
						closePopup();
					}
				});
				
				
			}
		}.start();
		
	}
	
	protected void closePopup() {
		dialog.close();		
	}
	
	protected void showPopup() {
		 
         dialog.initModality(Modality.APPLICATION_MODAL);
         dialog.initOwner(primaryStage);
         
         VBox dialogVbox = new VBox(20);
         dialogVbox.getChildren().add(text);
         Scene dialogScene = new Scene(dialogVbox, 300, 200);
         dialog.setScene(dialogScene);
         dialog.show();		
	}

	@Override
	public void progressMsg(String msg) {
		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				text.setText(msg);
			}
		});
	}

	@Override
	public void progressPercent(int percent) {
		// TODO Auto-generated method stub
		
	}

}
