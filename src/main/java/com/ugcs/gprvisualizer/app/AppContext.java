package com.ugcs.gprvisualizer.app;

import java.util.HashSet;
import java.util.Set;

import com.ugcs.gprvisualizer.app.intf.Status;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppContext {
	
	public static boolean PRODUCTION = true;

	public static Stage stage;
	public static Scene scene;
	public static Model model;
	public static Status status;
	
	private static Set<SmthChangeListener> smthListener = new HashSet<>();
	
	public static void setItems(Set<SmthChangeListener> it) {
		smthListener = it;
	}
	
	public static void notifyAll(WhatChanged changed) {
		
		Platform.runLater(() -> {
				for (SmthChangeListener lst : AppContext.smthListener) {
					try {
						lst.somethingChanged(changed);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		});		
	}	
}
