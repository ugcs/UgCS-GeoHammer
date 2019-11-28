package com.github.thecoldwine.sigrun.common.ext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public interface ByteBufferProducer {

	ByteBuffer read(BlockFile blockFile) throws IOException;
}
