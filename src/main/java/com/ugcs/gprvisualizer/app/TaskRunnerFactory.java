package com.ugcs.gprvisualizer.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.app.intf.Status;

@Component
public class TaskRunnerFactory {

	@Autowired
	private Status status;
	
	public TaskRunner get(ProgressTask task) {
		
		TaskRunner tr = new TaskRunner(status, task);
		
		
		return tr;
	}
}
