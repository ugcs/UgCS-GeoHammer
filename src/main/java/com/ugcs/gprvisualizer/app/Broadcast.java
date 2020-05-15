package com.ugcs.gprvisualizer.app;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ugcs.gprvisualizer.draw.SmthChangeListener;
import com.ugcs.gprvisualizer.draw.WhatChanged;

import javafx.application.Platform;

@Component
public class Broadcast {

	@Autowired
	private Set<SmthChangeListener> items;
	
	@PostConstruct
	public void init() {
		AppContext.setItems( getItems() );
	}
	
	public Set<SmthChangeListener> getItems(){
		return items;
	}
	
	public void notifyAll(WhatChanged changed) {
		
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
		
				for (SmthChangeListener lst : items) {
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
