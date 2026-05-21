package com.voys.memo.application;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class TemporaryTitleGenerator {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private final Clock clock;

	public TemporaryTitleGenerator(Clock clock) {
		this.clock = clock;
	}

	public String generate() {
		return "Recording " + LocalDateTime.now(clock).format(FORMATTER);
	}
}
