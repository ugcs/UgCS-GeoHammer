package com.github.thecoldwine.sigrun.converters;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TC2BIConverter implements SeismicValuesConverter{

	@Override
	public float[] convert(byte[] bytes) {
		
		
        float[] result = new float[bytes.length / 2];

        ByteBuffer bits = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < result.length; i++) {
        	int val = bits.getShort();
            result[i] = val;
        }
		
		return result;
	}

	public ByteBuffer valuesToByteBuffer(float values[]) {
		
		ByteBuffer bb = ByteBuffer.allocate(values.length * 2).order(ByteOrder.LITTLE_ENDIAN);
		for(int i =0; i < values.length; i++) {
			bb.putShort((short)values[i]);
		}
		
		return bb;
	}
	
}
