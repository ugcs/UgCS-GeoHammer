package com.ugcs.gprvisualizer.gpr;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RecalculationController {
	//private int renderCounter = 0;
	
	private AtomicReference<RecalculationLevel> refLevel = new AtomicReference<>();
	private boolean activeRender = false;

	private Consumer<RecalculationLevel> consumer;
	public RecalculationController(Consumer<RecalculationLevel> consumer) {
		this.consumer = consumer;
	}
	
	public void render(RecalculationLevel level) {
		
		synchronized (RecalculationController.this) {
			if(activeRender) {
				
				refLevel.set(max(refLevel.get(), level));
				return;
			}else {
				activeRender = true;
			}
		}
		
		new Thread() {
			public void run() {
				boolean contin = false;
				RecalculationLevel lev = level;
				do {
					
					try{
						consumer.accept(lev);
					}catch(Exception e) {
						e.printStackTrace();						
					}
					
					synchronized (RecalculationController.this) {
						lev = refLevel.get();
						if(lev != null) {
							refLevel.set(null);
							contin = true;
						}else {
							activeRender = false;
							contin = false;
						}
					}
					
				}while(contin); 
			}

		}.start();
		
	}

	public boolean isEnquiued() {
		return refLevel.get() != null;
	}
	
	private RecalculationLevel max(RecalculationLevel recalculationLevel, RecalculationLevel level) {
		
		if(recalculationLevel == null) {
			return level;
		}
		if(level == null) {
			return recalculationLevel;
		}
		
		return recalculationLevel.getLevel() > level.getLevel() ? recalculationLevel : level;
	}
	
	
	
}
