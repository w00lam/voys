package com.voys.shared.api;

import java.util.Map;

public record ApiErrorResponse(
	String code,
	String message,
	Map<String, Object> details
) {
	public ApiErrorResponse(String code, String message) {
		this(code, message, Map.of());
	}
}
