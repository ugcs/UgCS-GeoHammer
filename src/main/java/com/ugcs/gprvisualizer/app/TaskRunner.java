package com.ugcs.gprvisualizer.app;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;

import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TaskRunner implements ProgressListener {
	private Stage primaryStage;
	private Stage dialog = new Stage();
	private ImageView loadingView = ResourceImageHolder.getImageView("loading.gif");
	private ProgressTask task;
	//private Text text = new Text("This is a Dialog");
	private VBox dialogVbox = new VBox(20);
	
	private Button closeButton = new Button("Close");
	
	public TaskRunner(Stage primaryStage, ProgressTask task) {
		this.primaryStage = primaryStage;
		this.task = task;
		
		
		closeButton.setOnAction(e -> {
			closePopup();
		});
		
		//new Image(this.getClass().getResource("java.gif").toExternalForm());
		
		dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        
        
        dialogVbox.getChildren().addAll(loadingView);
		
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
				
				try {
					task.run(TaskRunner.this);

					progressMsg("");
					Platform.runLater(new Runnable(){
						@Override
						public void run() {
							closePopup();
						}
					});
				}catch(Exception e) {
					e.printStackTrace();
					progressMsg("Error: " + e.getMessage());
				}
				
				
			}
		}.start();
		
	}
	
	protected void closePopup() {
		//dialog.close();
		
		primaryStage.getScene().setCursor(Cursor.DEFAULT);
	}
	
	protected void showPopup() {
		 
		
		primaryStage.getScene().setCursor(Cursor.WAIT);
        //Scene dialogScene = new Scene(dialogVbox, loadingView.getImage().getWidth(), loadingView.getImage().getHeight());
        //dialog.setScene(dialogScene);
        //dialog.show();		
	}

	@Override
	public void progressMsg(String msg) {
		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				AppContext.statusBar.showProgressText(msg);
			}
		});
	}

	@Override
	public void progressPercent(int percent) {
		// TODO Auto-generated method stub
		
	}

}
