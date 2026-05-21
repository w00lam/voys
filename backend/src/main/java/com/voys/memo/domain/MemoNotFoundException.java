package com.voys.memo.domain;

import java.util.UUID;

public class MemoNotFoundException extends RuntimeException {

	public MemoNotFoundException(UUID memoId) {
		super("Memo was not found: " + memoId);
	}
}
