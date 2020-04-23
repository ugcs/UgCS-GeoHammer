package com.ugcs.gprvisualizer.app;

import java.util.HashSet;
import java.util.Set;

import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.LevelFilter;

import javafx.application.Platform;
import javafx.stage.Stage;

public class AppContext {

	public static Stage stage;
	
	public static Model model;
	public static Loader loader;
	public static Saver saver;
	public static LevelFilter levelFilter;
	public static PluginRunner pluginRunner;
	public static Navigator navigator;
	public static StatusBar statusBar;
	public static ProfileView cleverImageView;
	
	public static Set<SmthChangeListener> smthListener = new HashSet<>();
	
	public static void notifyAll(WhatChanged changed) {
		
		if(!changed.isJustdraw()) {
			//System.out.println("[notification]" + changed.toString());
		}
		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {

		
				for (SmthChangeListener lst : AppContext.smthListener) {
					try {
						lst.somethingChanged(changed);
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
		
			}
		});		
	}	
}
