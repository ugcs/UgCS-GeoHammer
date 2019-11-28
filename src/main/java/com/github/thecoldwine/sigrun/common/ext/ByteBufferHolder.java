package com.github.thecoldwine.sigrun.common.ext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferHolder implements ByteBufferProducer {

	Trace trace;
	public ByteBufferHolder(Trace trace) {
		
		this.trace = trace;
	}
	
	@Override
	public ByteBuffer read(BlockFile blockFile) throws IOException {

		return valuesToByteBuffer(trace.getNormValues());
	}

	public static ByteBuffer valuesToByteBuffer(float values[]) {
		
		ByteBuffer bb = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN);
		for(int i =0; i < values.length; i++) {
			bb.putInt((int)values[i]);
		}
		
		return bb;
	}
	
	
}
