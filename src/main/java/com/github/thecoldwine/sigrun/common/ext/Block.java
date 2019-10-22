package com.github.thecoldwine.sigrun.common.ext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import com.github.thecoldwine.sigrun.common.TextHeader;

public class Block {

	private FileChannel chan;
	private int start;
	private int length;

	
	public Block(FileChannel chan, int start, int length) {
		this.chan = chan;
		this.start = start;
		this.length = length;
	}
	
	public Block(Block prev, int length) {
		this.chan = prev.chan;
		this.start = prev.getFinishPos();
		this.length = length;
	}
	
	public ByteBuffer read() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(length);
		
		chan.position(start);
	    if (chan.read(buf) != length) {
	    	throw new IOException();
	    }
	    
	    return buf;
	}
	
	public int getFinishPos() {
		return start+length;
	}
	

	
}
