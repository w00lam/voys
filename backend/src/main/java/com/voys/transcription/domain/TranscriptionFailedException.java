package com.voys.transcription.domain;

public class TranscriptionFailedException extends RuntimeException {

	public TranscriptionFailedException(String message) {
		super(message);
	}

	public TranscriptionFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
