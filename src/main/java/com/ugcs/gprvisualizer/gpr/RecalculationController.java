package com.ugcs.gprvisualizer.gpr;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RecalculationController {
		
	private AtomicReference<Boolean> refLevel = new AtomicReference<>();
	private boolean activeRender = false;

	private Consumer<Void> consumer;
	
	public RecalculationController(Consumer<Void> consumer) {
		this.consumer = consumer;
	}
	
	public void render() {
		
		synchronized (RecalculationController.this) {
			if (activeRender) {
				
				refLevel.set(true);
				return;
			} else {
				activeRender = true;
			}
		}
		
		new Thread() {
			public void run() {
				boolean contin = false;
				
				do {
					
					try {
						consumer.accept(null);
					} catch (Exception e) {
						e.printStackTrace();						
					}
					
					synchronized (RecalculationController.this) {
						Boolean lev = refLevel.get();
						if (lev != null) {
							refLevel.set(null);
							contin = true;
						} else {
							activeRender = false;
							contin = false;
						}
					}
					
				} while (contin); 
			}

		}.start();
		
	}

	public boolean isEnquiued() {
		return refLevel.get() != null;
	}
	
}
