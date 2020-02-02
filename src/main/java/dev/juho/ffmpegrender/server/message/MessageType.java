package dev.juho.ffmpegrender.server.message;

public enum MessageType {

	GOODBYE,
	FILE_INFO,
	SET_UUID,
	SET_RENDER_OPTIONS,
	VIDEO_RENDERED,
	BLOCK_NEW_VIDEOS,
	SET_LOCAL_CLIENT,
	UPDATE_RENDER_PROGRESS,
	SET_CLIENT_TYPE,

}
