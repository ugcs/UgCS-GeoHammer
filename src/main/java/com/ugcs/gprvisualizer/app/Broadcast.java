package com.ugcs.gprvisualizer.app;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.draw.Change;
import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;

import javafx.application.Platform;

@Component
public class Broadcast implements InitializingBean {

	@Autowired
	private Set<SmthChangeListener> items;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		AppContext.setItems(getItems());
	}
	
	public Set<SmthChangeListener> getItems() {
		return items;
	}
	
	public void notifyAll(WhatChanged changed) {
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
		
				for (SmthChangeListener lst : items) {
					try {
						lst.somethingChanged(changed);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		
			}
		});		
	}

	public void fileSelected(SgyFile file) {
		notifyAll(new FileSelected(file));
	}

    public void fileSelected(List<SgyFile> files) {
		notifyAll(new FileSelected(files));
    }
	
}
