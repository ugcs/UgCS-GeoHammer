package com.github.thecoldwine.sigrun.common.ext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class BlockFile {
	
	private FileChannel chan;
	private int position = 0;
	
	public static BlockFile open(File file) throws FileNotFoundException {
		
		FileChannel chan = new FileInputStream(file).getChannel();
		
		return new BlockFile(chan);
	}
	
	private BlockFile(FileChannel chan) {
		this.chan = chan;
	}
	
	public Block next(int length) {
		Block block = new Block(chan, position, length);
		position = block.getFinishPos();
		
		return block;
	}
	
	public boolean hasNext() {
		try {
			return position < chan.size();
		} catch (IOException e) {
			return false;			
		}
	}
	

}
