package com.ugcs.gprvisualizer.app;

import java.util.HashSet;
import java.util.Set;

import com.ugcs.gprvisualizer.app.intf.Status;
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
	
}