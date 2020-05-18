package com.ugcs.gprvisualizer.app;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.app.intf.Status;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TaskRunner implements ProgressListener {
	
	private Status status;
	private Stage primaryStage = AppContext.stage;
	private Stage dialog = new Stage();
	private ImageView loadingView = ResourceImageHolder.getImageView("loading.gif");
	private ProgressTask task;
	//private Text text = new Text("This is a Dialog");
	private VBox dialogVbox = new VBox(20);
	
	private Button closeButton = new Button("Close");
	
	public TaskRunner(Status status, ProgressTask task) {
		this.status = status;
		
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
		showPopup();
		
		new Thread() {
			public void run() {
				try {
					task.run(TaskRunner.this);

				} catch (Exception e) {
					e.printStackTrace();
					progressMsg("Error: " + e.getMessage());
				}

				closePopup();
			}
		}.start();
	}
	
	protected void closePopup() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				primaryStage.getScene().setCursor(Cursor.DEFAULT);
			}
		});
	}
	
	protected void showPopup() {
		 
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Sout.p(" showPopup() ");
				
				primaryStage.getScene().setCursor(Cursor.WAIT);
			}
		});
	}

	@Override
	public void progressMsg(String msg) {
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				status.showProgressText(msg);
			}
		});
	}

	@Override
	public void progressPercent(int percent) {
		
	}

}
