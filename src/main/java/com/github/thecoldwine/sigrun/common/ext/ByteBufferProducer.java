package com.github.thecoldwine.sigrun.common.ext;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ByteBufferProducer {

	ByteBuffer read() throws IOException;
}
