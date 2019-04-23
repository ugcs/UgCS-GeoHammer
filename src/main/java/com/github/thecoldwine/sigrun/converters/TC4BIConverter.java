package com.github.thecoldwine.sigrun.converters;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TC4BIConverter implements SeismicValuesConverter{

	@Override
	public float[] convert(byte[] bytes) {
		
		
        float[] result = new float[bytes.length / 4];

        ByteBuffer bits = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < result.length; i++) {
        	
        	int val = bits.getInt();
        	//System.out.print(", " + val);
            result[i] = val;
            
        }
		
		//int i = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
		
		return result;
	}

}
