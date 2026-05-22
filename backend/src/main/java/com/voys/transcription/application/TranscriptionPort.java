package com.voys.transcription.application;

import java.nio.file.Path;
import java.util.List;

public interface TranscriptionPort {

	TranscriptionResult transcribe(TranscriptionRequest request);

	record TranscriptionRequest(
		String memoId,
		Path audioPath,
		String language
	) {
	}

	record TranscriptionResult(
		String text,
		List<TranscriptionSegment> segments
	) {
		public TranscriptionResult(String text) {
			this(text, List.of());
		}
	}

	record TranscriptionSegment(
		double startSeconds,
		double endSeconds,
		String text
	) {
	}
}
