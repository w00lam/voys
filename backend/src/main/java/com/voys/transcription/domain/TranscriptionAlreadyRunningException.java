package com.voys.transcription.domain;

import java.util.UUID;

public class TranscriptionAlreadyRunningException extends RuntimeException {

	public TranscriptionAlreadyRunningException(UUID memoId) {
		super("Transcription for memo %s is already running.".formatted(memoId));
	}
}
