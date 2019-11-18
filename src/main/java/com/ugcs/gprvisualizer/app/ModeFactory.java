package com.ugcs.gprvisualizer.app;

import java.util.List;

import javafx.scene.Node;

public interface ModeFactory {

	void show();
	Node getCenter();
	List<Node> getRight();
	
}
