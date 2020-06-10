package com.ugcs.gprvisualizer.app;

public class Sout {

	public static boolean developMode = true;

	public static void p(String msg) {
		if (developMode) {
			System.out.println(msg);
		}
	}
	
	public static String d(double d) {
		return String.format(" %.3f ", d);
	
	}
	
	public static void sleep(int milsec) {
		if (developMode) {		
			try {
				Thread.sleep(milsec);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
}
