package com.ugcs.gprvisualizer.app;

import java.util.ArrayList;
import java.util.List;

import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.math.LevelFilter;

public class AppContext {

	
	public static Loader loader;
	public static Saver saver;
	public static LevelFilter levelFilter;
	
	public static List<SmthChangeListener> smthListener = new ArrayList<>();
	
}
