package com.voys.search.application;

public record SearchResult(
	String memoId,
	String title,
	String matchType,
	String snippet,
	String transcriptionStatus
) {}
