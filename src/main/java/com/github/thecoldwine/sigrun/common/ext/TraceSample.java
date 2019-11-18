package com.github.thecoldwine.sigrun.common.ext;

public class TraceSample {

	private int trace;
	private int sample;
	
	public TraceSample(int trace, int sample) {
		
		this.setTrace(trace);
		this.setSample(sample);
		
	}

	public int getTrace() {
		return trace;
	}

	public void setTrace(int trace) {
		this.trace = trace;
	}

	public int getSample() {
		return sample;
	}

	public void setSample(int sample) {
		this.sample = sample;
	}
}
