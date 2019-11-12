package com.ugcs.gprvisualizer.app;

import java.util.List;

import javafx.scene.Node;

public interface ModeFactory {

	void show(int width, int height);
	Node getCenter();
	List<Node> getRight();
	
}
