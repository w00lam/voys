package com.voys.memo.domain;

public class InvalidMemoTitleException extends RuntimeException {

	public InvalidMemoTitleException(String message) {
		super(message);
	}
}
