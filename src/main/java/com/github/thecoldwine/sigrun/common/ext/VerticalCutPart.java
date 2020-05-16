package com.github.thecoldwine.sigrun.common.ext;

public class VerticalCutPart {

	private int startTrace;
	private int finishTrace;
	private int maxSamples;
	
	public int getTraces() {
		return finishTrace - startTrace;
	}
	
	public int localToGlobal(int trace) {
		return trace + startTrace;
	}

	public int globalToLocal(int trace) {
		return trace - startTrace;
	}
	
	public int normTrace(int trace) {
		return trace;
	}
	
	public int getStartTrace() {
		return startTrace;
	}
	
	public void setStartTrace(int startTrace) {
		this.startTrace = startTrace;	
	}
	
	public int getFinishTrace() {
		return finishTrace;
	}
	
	public void setFinishTrace(int finishTrace) {
		this.finishTrace = finishTrace;
	}

	public int getMaxSamples() {
		return maxSamples;
	}

	public void setMaxSamples(int maxSamples) {
		this.maxSamples = maxSamples;
	}
	
	
}
