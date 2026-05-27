package com.voys.notes.domain;

import java.util.List;

public record GeneratedNoteDraft(
	String summary,
	List<String> keyPoints,
	List<String> actionItems
) {}
