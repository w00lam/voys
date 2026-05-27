package com.voys.notes.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.voys.notes.domain.GeneratedNoteDraft;

@Component
public class GeneratedNoteGenerator {

	public GeneratedNoteDraft generate(String text) {
		if (text == null || text.isBlank()) {
			return new GeneratedNoteDraft("", List.of(), List.of());
		}

		String[] lines = text.split("(?<=[.!?])\\s+|\\n+");

		List<String> keyPoints = new ArrayList<>();
		List<String> actionItems = new ArrayList<>();
		String summary = "";

		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.isEmpty()) {
				continue;
			}

			if (summary.isEmpty()) {
				summary = trimmed;
			}

			if (keyPoints.size() < 3) {
				keyPoints.add(trimmed);
			}

			String lower = trimmed.toLowerCase();
			if (lower.contains("action") ||
					lower.contains("todo") ||
					lower.contains("follow up") ||
					lower.contains("follow-up") ||
					lower.contains("next") ||
					lower.contains("\uD574\uC57C") ||
					lower.contains("\uD655\uC778")) {
				actionItems.add(trimmed);
			}
		}

		return new GeneratedNoteDraft(summary, keyPoints, actionItems);
	}
}
