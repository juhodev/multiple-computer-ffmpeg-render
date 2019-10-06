package dev.juho.ffmpegrender.server.client.reader;

import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.events.events.MessageEvent;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class MessageData implements ReaderData {

	private ByteBuffer lengthBuffer;
	private ByteBuffer messageDataBuffer;
	private int length;
	private boolean isFull;

	public MessageData() {
		this.lengthBuffer = ByteBuffer.allocate(4);
		this.length = 0;
		this.isFull = false;
	}

	@Override
	public void write(byte[] bytes, int offset, int length) {
		if (lengthBuffer.position() >= 4) {
			if (messageDataBuffer == null) {
				createMessageDataBuffer();
			}

			writeMessageData(bytes, offset, length);
		} else {
			int bytesToFill = 4 - lengthBuffer.position();
			lengthBuffer.put(bytes, offset, bytesToFill);
			offset += bytesToFill;
			this.length += bytesToFill;
			createMessageDataBuffer();

			writeMessageData(bytes, offset, length);
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
		return;
	}

	private void createMessageDataBuffer() {
		int messageLength = lengthBuffer.getInt(0);
		messageDataBuffer = ByteBuffer.allocate(messageLength);
	}

	private void writeMessageData(byte[] bytes, int offset, int length) {
		int messageLength = lengthBuffer.getInt(0);
		int dataLeftInBuffer = length - offset;
		if (dataLeftInBuffer > messageLength) {
			messageDataBuffer.put(bytes, offset, messageLength);
			this.length += messageLength;
		} else {
			messageDataBuffer.put(bytes, offset, dataLeftInBuffer);
			this.length += dataLeftInBuffer;
		}

		if (messageDataBuffer.position() == messageLength) {
			isFull = true;
			String messageDataStr = new String(messageDataBuffer.array());
			JSONObject message = new JSONObject(messageDataStr);

			Message msg = new Message(messageLength, UUID.fromString(message.getString("sender")), message.getJSONObject("data"), MessageType.valueOf(message.getString("type")));
			EventBus.getInstance().publish(new MessageEvent(msg));
		}
	}
}
