package com.ugcs.gprvisualizer.app;

public class CmdTestProducer {

	
	public static void main(String[] args) {
		
		for(int i =0; i < 20; i++) {
			
			
			System.out.println("step " + i);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}
