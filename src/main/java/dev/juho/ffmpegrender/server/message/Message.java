package dev.juho.ffmpegrender.server.message;

import dev.juho.ffmpegrender.server.client.MessageIdentifier;
import dev.juho.ffmpegrender.server.client.reader.FileData;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class Message {

	private int length;
	private UUID sender;
	private JSONObject data;
	private MessageType type;

	public Message(int length, UUID sender, JSONObject data, MessageType type) {
		this.length = length;
		this.sender = sender;
		this.data = data;
		this.type = type;
	}

	public MessageType getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public UUID getSender() {
		return sender;
	}

	public JSONObject getData() {
		return data;
	}

	public static Message parse(byte[] bytes) {
		ByteBuffer lengthBuffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 1, 5));
		int length = lengthBuffer.getInt();

		ByteBuffer stringBuffer = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 5, 5 + length));
		String jsonStr = new String(stringBuffer.array());
		JSONObject message = new JSONObject(jsonStr);

		UUID uuid = null;

		if (!message.getString("sender").equalsIgnoreCase("not-set")) {
			uuid = UUID.fromString(message.getString("sender"));
		}

		return new Message(length, uuid, message.getJSONObject("data"), MessageType.valueOf(message.getString("type")));
	}

	public static Message build(MessageType type, UUID uuid, JSONObject data) {
		JSONObject message = new JSONObject();
		message.put("sender", uuid.toString());
		message.put("type", type);
		message.put("data", data);

		return new Message(message.toString().getBytes().length, uuid, message, type);
	}

	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(FileData.MESSAGE_IDENTIFIER_SIZE + FileData.MESSAGE_LENGTH_BUFFER_SIZE + length);
		buffer.put(MessageIdentifier.MESSAGE);
		buffer.putInt(length);
		buffer.put(data.toString().getBytes());
		return buffer.array();
	}

}
