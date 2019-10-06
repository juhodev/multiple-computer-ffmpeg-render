package dev.juho.ffmpegrender.server.client.reader;

import dev.juho.ffmpegrender.utils.Logger;

import java.io.*;
import java.nio.ByteBuffer;

public class FileData implements ReaderData {

	public final static int MESSAGE_IDENTIFIER_SIZE = 1;
	public final static int NAME_LENGTH_BUFFER_SIZE = 4;
	public final static int MESSAGE_LENGTH_BUFFER_SIZE = 4;
	public final static int FILE_LENGTH_BUFFER_SIZE = 8;

	/**
	 * File info
	 * <p>
	 * 1 byte identifier
	 * 4 bytes name length
	 * x bytes name
	 * 8 bytes file length
	 * x bytes file
	 */

	private String rootFolder;
	private boolean closed;

	private ByteBuffer nameLengthBuffer;
	private ByteBuffer nameBuffer;
	private ByteBuffer fileLengthBuffer;
	private int length;
	private boolean isFull;

	private BufferedOutputStream fileOS;
	private int fileWritten;


	public FileData(String rootFolder) {
		this.rootFolder = rootFolder;
		this.nameLengthBuffer = ByteBuffer.allocate(NAME_LENGTH_BUFFER_SIZE);
		this.fileLengthBuffer = ByteBuffer.allocate(FILE_LENGTH_BUFFER_SIZE);
		this.length = 0;
		this.fileWritten = 0;
		this.isFull = false;
		this.closed = false;
	}

	@Override
	public void write(byte[] bytes, int offset, int length) {
		if (nameLengthBuffer.position() < NAME_LENGTH_BUFFER_SIZE) {
			offset += writeToNameLengthBuffer(bytes, offset, length);

			if (nameLengthBuffer.position() >= NAME_LENGTH_BUFFER_SIZE) {
				createNameBuffer();
			}

			if (offset >= length) {
				return;
			}
		}

		if (nameBuffer.position() < nameLengthBuffer.getInt(0)) {
			offset += writeToNameBuffer(bytes, offset, length);

			if (offset >= length) {
				return;
			}
		}


		if (fileLengthBuffer.position() < FILE_LENGTH_BUFFER_SIZE) {
			offset += writeToFileLengthBuffer(bytes, offset, length);

			if (offset >= length) {
				return;
			}
		}


		if (fileOS == null) {
			try {
				setupFileStreams();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		try {
			offset += writeToFile(bytes, offset, length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public boolean isFull() {
		return isFull;
	}

	@Override
	public void resetLength() {
		this.length = 0;
	}

	@Override
	public void close() throws IOException {
		if (!closed) {
			closed = true;
			fileOS.flush();
			fileOS.close();
		}
	}

	private int writeToFile(byte[] bytes, int offset, int length) throws IOException {
		long bytesToFill = fileLengthBuffer.getLong(0) - fileWritten;
		int dataLeftInBuffer = length - offset;
		if (bytesToFill > dataLeftInBuffer) {
			fileOS.write(bytes, offset, dataLeftInBuffer);
			fileWritten += dataLeftInBuffer;
			this.length += dataLeftInBuffer;
			updateIsFull();
			return dataLeftInBuffer;
		} else {
			fileOS.write(bytes, offset, (int) bytesToFill);
			fileWritten += bytesToFill;
			this.length += bytesToFill;
			updateIsFull();
			return (int) bytesToFill;
		}
	}

	private void updateIsFull() {
		isFull = fileWritten >= fileLengthBuffer.getLong(0);

		if (isFull) {
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void setupFileStreams() throws FileNotFoundException {
		String fileName = new String(nameBuffer.array());
		File file = new File(rootFolder + "/" + fileName);
		file.getParentFile().mkdirs();

		fileOS = new BufferedOutputStream(new FileOutputStream(file));
		Logger.getInstance().log(Logger.DEBUG, "Writing to " + file.getAbsolutePath());
	}

	private int writeToNameBuffer(byte[] bytes, int offset, int length) {
		int bytesToFill = nameLengthBuffer.getInt(0) - nameBuffer.position();
		int availableBytesToFill = availableBytesToFill(offset, length, bytesToFill);
		nameBuffer.put(bytes, offset, availableBytesToFill);
		this.length += bytesToFill;
		return availableBytesToFill;
	}

	private int writeToNameLengthBuffer(byte[] bytes, int offset, int length) {
		int bytesToFill = NAME_LENGTH_BUFFER_SIZE - nameLengthBuffer.position();
		int availableBytesToFill = availableBytesToFill(offset, length, bytesToFill);
		nameLengthBuffer.put(bytes, offset, availableBytesToFill);
		this.length += bytesToFill;
		return availableBytesToFill;
	}

	private int writeToFileLengthBuffer(byte[] bytes, int offset, int length) {
		int bytesToFill = FILE_LENGTH_BUFFER_SIZE - fileLengthBuffer.position();
		int availableBytesToFill = availableBytesToFill(offset, length, bytesToFill);
		fileLengthBuffer.put(bytes, offset, availableBytesToFill);
		this.length += bytesToFill;
		return availableBytesToFill;
	}

	private void createNameBuffer() {
		int nameLength = nameLengthBuffer.getInt(0);
		nameBuffer = ByteBuffer.allocate(nameLength);
	}

	private int availableBytesToFill(int offset, int length, int bufferSize) {
		if (offset + bufferSize > length) {
			return length - offset;
		}

		return bufferSize;
	}

}
