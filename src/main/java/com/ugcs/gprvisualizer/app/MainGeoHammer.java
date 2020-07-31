package com.ugcs.gprvisualizer.app;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.github.thecoldwine.sigrun.common.ext.ResourceImageHolder;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class MainGeoHammer extends Application {

	private static final String TITLE_VERSION = "UgCS GeoHammer v.1.0.7";
	
	private Model model;
	private RootControls rootControls;
	private ApplicationContext context; 
	
	public MainGeoHammer() {
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
    public void init() {
		//create all classes
		context = new ClassPathXmlApplicationContext("spring.xml");
		 
		model = context.getBean(Model.class);
		  
		rootControls = context.getBean(RootControls.class);
		
    }	

	@Override
	public void start(Stage stage) throws Exception {

		AppContext.stage = stage;

        stage.getIcons().add(ResourceImageHolder.IMG_LOGO24);
	
        stage.setTitle(TITLE_VERSION);
		
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
		Scene scene = new Scene(rootControls.getSceneContent(), screenSize.getWidth()-80, 700);
		//scene.set
		stage.setScene(scene);
		//stage.setMaximized(true);
		
		stage.setX(0);
		stage.setY(0);
		stage.show();

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent t) {
		    	
	        	if (model.stopUnsaved()) {
	        		t.consume();
	        		return;
	        	}        	
		    	
		        Platform.exit();
		        System.exit(0);
		    }
		});		
		
		
		//load files if they were given in parameters 
		if (!getParameters().getRaw().isEmpty()) {
			String name = getParameters().getRaw().get(0);
			List<File> f = Arrays.asList(new File(name));			
			rootControls.getLoader().loadWithNotify(f, emptyListener);
		}
	}


	private static final ProgressListener emptyListener = new ProgressListener() {
		@Override
		public void progressPercent(int percent) {}
		
		@Override
		public void progressMsg(String msg) {}

		@Override
		public void progressSubMsg(String msg) {
			// TODO Auto-generated method stub
			
		}
	};

}
