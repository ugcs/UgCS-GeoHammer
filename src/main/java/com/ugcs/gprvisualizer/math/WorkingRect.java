package com.ugcs.gprvisualizer.math;

import com.github.thecoldwine.sigrun.common.ext.SgyFile;

public class WorkingRect {

	private SgyFile file;
	private int traceFrom;
	private int traceTo;
	private int smpFrom;
	private int smpTo;

	private int tracePin;
	private int smpPin;
	
	public WorkingRect(SgyFile file,
		int traceFrom,
		int traceTo,
		int smpFrom,
		int smpTo,
		int tracePin,
		int smpPin) {
		
		this.file = file;
		this.traceFrom = traceFrom;
		this.traceTo = traceTo;
		this.smpFrom = smpFrom;
		this.smpTo = smpTo;
		this.tracePin = tracePin;
		this.smpPin = smpPin;
	}

	public SgyFile getFile() {
		return file;
	}

	public int getTraceFrom() {
		return traceFrom;
	}

	public int getTraceTo() {
		return traceTo;
	}

	public int getSmpFrom() {
		return smpFrom;
	}

	public int getSmpTo() {
		return smpTo;
	}

	public int getTracePin() {
		return tracePin;
	}

	public int getSmpPin() {
		return smpPin;
	}
	
	
	
}
