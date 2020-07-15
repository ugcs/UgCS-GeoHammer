package com.ugcs.gprvisualizer.app;

public interface ProgressListener {

	void progressMsg(String msg);
	void progressSubMsg(String msg);
	
	void progressPercent(int percent);
}
