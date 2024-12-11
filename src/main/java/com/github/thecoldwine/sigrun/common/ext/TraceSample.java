package com.github.thecoldwine.sigrun.common.ext;

public record TraceSample (int trace, int sample) {
	public int getTrace() {
		return trace;
	}

	public int getSample() {
		return sample;
	}
}