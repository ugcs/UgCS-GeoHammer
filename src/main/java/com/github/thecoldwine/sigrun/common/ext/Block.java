package com.github.thecoldwine.sigrun.common.ext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import com.github.thecoldwine.sigrun.common.TextHeader;

public class Block implements ByteBufferProducer {

	//private FileChannel chan;
	private int start;
	private int length;

	
	public Block(int start, int length) {
		
		this.start = start;
		this.length = length;
	}
	
	public Block(Block prev, int length) {		
		this.start = prev.getFinishPos();
		this.length = length;
	}
	
	public ByteBuffer read(BlockFile blockFile) throws IOException {
		
		ByteBuffer buf = ByteBuffer.allocate(length);
		
		blockFile.getChannel().position(start);
	    if (blockFile.getChannel().read(buf) != length) {
	    	throw new IOException();
	    }
	    
	    return buf;
	}
	
	public int getFinishPos() {
		return start+length;
	}
	

	
}
