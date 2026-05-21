package com.voys.search.domain;

public class InvalidSearchQueryException extends RuntimeException {
	public InvalidSearchQueryException(String message) {
		super(message);
	}
}
