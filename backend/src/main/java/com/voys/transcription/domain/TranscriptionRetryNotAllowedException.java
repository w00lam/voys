package com.voys.transcription.domain;

import java.util.UUID;

public class TranscriptionRetryNotAllowedException extends RuntimeException {

	public TranscriptionRetryNotAllowedException(UUID memoId, String status) {
		super("Transcription retry is allowed only for failed memos. memoId=%s status=%s".formatted(memoId, status));
	}
}
