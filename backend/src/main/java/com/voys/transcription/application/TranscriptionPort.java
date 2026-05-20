package com.voys.transcription.application;

import java.nio.file.Path;

public interface TranscriptionPort {

	TranscriptionResult transcribe(TranscriptionRequest request);

	record TranscriptionRequest(
		String memoId,
		Path audioPath,
		String language
	) {
	}

	record TranscriptionResult(
		String text
	) {
	}
}

