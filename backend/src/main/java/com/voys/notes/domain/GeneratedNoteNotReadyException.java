package com.voys.notes.domain;

import java.util.UUID;

public class GeneratedNoteNotReadyException extends RuntimeException {

	public GeneratedNoteNotReadyException(UUID memoId) {
		super("completed transcript is not available for memo: " + memoId);
	}
}
