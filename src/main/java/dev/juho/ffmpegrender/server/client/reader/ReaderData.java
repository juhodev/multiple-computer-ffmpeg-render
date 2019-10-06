package dev.juho.ffmpegrender.server.client.reader;

import java.io.IOException;

public interface ReaderData<T> {

	void write(byte[] bytes, int offset, int length);

	int getLength();

	boolean isFull();

	void resetLength();

	void close() throws IOException;

}
