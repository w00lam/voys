package com.voys.memo.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class TemporaryTitleGeneratorTests {

	@Test
	void generateUsesCurrentDateAndTime() {
		Clock clock = Clock.fixed(Instant.parse("2026-05-21T01:23:00Z"), ZoneId.of("UTC"));
		TemporaryTitleGenerator generator = new TemporaryTitleGenerator(clock);

		assertThat(generator.generate()).isEqualTo("Recording 2026-05-21 01:23");
	}
}
