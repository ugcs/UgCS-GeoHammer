package com.ugcs.gprvisualizer.app;

import java.util.ArrayList;
import java.util.List;

import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;
import com.ugcs.gprvisualizer.gpr.Model;
import com.ugcs.gprvisualizer.math.LevelFilter;

public class AppContext {

	
	public static Model model;
	public static Loader loader;
	public static Saver saver;
	public static LevelFilter levelFilter;
	public static PluginRunner pluginRunner;
	public static Navigator navigator;
	
	public static List<SmthChangeListener> smthListener = new ArrayList<>();
	
	public static void notifyAll(WhatChanged changed) {
		for (SmthChangeListener lst : AppContext.smthListener) {
			try {
				lst.somethingChanged(changed);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
