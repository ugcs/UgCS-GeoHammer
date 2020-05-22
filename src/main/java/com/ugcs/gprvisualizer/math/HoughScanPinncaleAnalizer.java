package com.ugcs.gprvisualizer.math;

import java.awt.image.BufferedImage;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;
import com.ugcs.gprvisualizer.app.auxcontrol.RulerTool;

public class HoughScanPinncaleAnalizer {

	boolean isPrintLog = false;
	
	public HoughHypFullness fullnessAnalizer = null;
	public StubPrepator additionalPreparator = new StubPrepator();
	
	private HoughDiscretizer discretizer = new HoughDiscretizer();
	
	public static class StubPrepator {
		public void mark(int tr, int smp, int xfd1, int xfd2) {
			// do nothing
		}
		
		public BufferedImage getImage() {
			return null;
		}
	}
	
	public HoughArray[] processRectAroundPin(SgyFile sgyFile, WorkingRect workingRect, double threshold) {
		HoughArray[] stores = new HoughArray[5];
		//init stores
		for (int i = 0; i < stores.length; i++) {
			stores[i] = new HoughArray(threshold);
		}
		
		//
		for (int smp = workingRect.getSmpFrom()-1; 
				smp <= workingRect.getSmpTo(); smp++) {
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("%3d | ", smp));
			
			double addValue = (smp == workingRect.getSmpFrom() ? 0.40 : 1.0); 
			
			for (int tr = workingRect.getTraceFrom();
					tr <= workingRect.getTraceTo(); tr++) {
				
				
				int edge = getEdge(sgyFile, tr, smp);
				if (tr == workingRect.getTracePin()) {
					continue;
				}
				
				double xf1 = getFactorX(sgyFile, 
						workingRect.getTracePin(), workingRect.getSmpPin(), 
						tr, smp + 0.01);
				//overlapping
				double xf2 = getFactorX(sgyFile,
						workingRect.getTracePin(), workingRect.getSmpPin(), 
						tr, smp + 1.99);
				
				int xfd1 = discretizer.transform(xf1);
				int xfd2 = discretizer.transform(xf2);
				
				stores[edge].add(xfd1, xfd2, addValue);
				

				additionalPreparator.mark(tr, smp, xfd1, xfd2);
				
				if (fullnessAnalizer != null) {
					fullnessAnalizer.add(tr - workingRect.getTracePin(), xfd1, xfd2, edge);
				}				
			}
			
			if (isPrintLog) {
				//Sout.p(sb.toString());
			}
		}
		
		for (HoughArray ha : stores) {
			ha.calculate();
		}
		
		return stores;
	}

	private double getFactorX(SgyFile sgyFile, int pinTr, double pinSmp, int tr, double smp) {

		double diagCm = RulerTool.distanceCm(sgyFile, pinTr, pinTr, 0, smp);
		double verticalCm = RulerTool.distanceCm(sgyFile, pinTr, pinTr, 0, pinSmp);
		double idealXCm = Math.sqrt(diagCm * diagCm - verticalCm * verticalCm);

		double realXCm = RulerTool.distanceCm(sgyFile, pinTr, tr, smp, smp);

		double factorX = idealXCm / realXCm;

		return factorX;
	}

	private int getEdge(SgyFile sgyFile, int tr, int smp) {

		return sgyFile.getTraces().get(tr).edge[smp];
	}

	/*
	 * 0 - except
	 */
//	private int discret(double factorX) {
//		// to 0-1
//		double norm = (factorX - HoughScan.DISCRET_FROM) 
//				/ (HoughScan.DISCRET_TO - HoughScan.DISCRET_FROM);
//
//		if (norm < 0) {
//			return -1;
//		}
//
//		if (norm > 1) {
//			return HoughScan.DISCRET_SIZE;
//		}
//
//		return (int) Math.round(norm * (HoughScan.DISCRET_SIZE - 1));
//	}

//	public void printForPoint(int tr, int smp) {
//
//		int pinSmp = model.getSettings().layer;
//		SgyFile file = model.getSgyFileByTrace(model.getVField().getSelectedTrace());
//		int pinTr = model.getVField().getSelectedTrace() - file.getOffset().getStartTrace();
//
//		double xf1 = getFactorX(file, pinTr, pinSmp, tr, smp + 0.02);
//		double xf2 = getFactorX(file, pinTr, pinSmp, tr, smp + 1.98);
//
//		int xfd1 = discret(xf1);
//		int xfd2 = discret(xf2);
//
//		Sout.p(" " + (tr - pinTr) + " " + (smp - pinSmp) 
//				+ " ->  " + Math.min(xfd1, xfd2) + " - "
//				+ Math.max(xfd1, xfd2));
//
//	}
	
}