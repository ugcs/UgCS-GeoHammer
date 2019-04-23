package com.ugcs.gprvisualizer.app;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainTestConcurency implements Callable<Void>{

	private String name;
	
	public static  void main(String[] args) throws InterruptedException {
		
		
		ExecutorService ex = Executors.newFixedThreadPool(10);
		
		for(int i = 0; i< 50000; i++) {
			ex.submit(new MainTestConcurency("thr " + i));
		}
		
		ex.awaitTermination(1, TimeUnit.HOURS);
		System.out.println("finish");
	}
	
	public MainTestConcurency (String name) {
		this.name = name;
	}
	

	@Override
	public Void call() throws Exception {
		System.out.println("start  " + name);
		
		Thread.sleep(1000);
		System.out.println("finish " + name);
		
		return null;
	}
	
	
	
	
	
}
