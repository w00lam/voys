package com.voys.transcription.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.voys.transcription.application.TranscriptionPort;

class LocalWhisperAdapterTests {

	@Test
	void buildCommandIncludesConfiguredModel() {
		LocalWhisperAdapter adapter = new LocalWhisperAdapter(
			"whisper",
			"tiny",
			"Korean",
			false,
			Path.of("build/transcripts"),
			60
		);

		assertThat(adapter.buildCommand(
			new TranscriptionPort.TranscriptionRequest("memo-1", Path.of("recording.webm"), "ko"),
			Path.of("build/transcripts/memo-1")
		)).containsSequence("--model", "tiny")
			.containsSequence("--fp16", "False")
			.containsSequence("--language", "ko");
	}

	@Test
	void buildCommandUsesDefaultLanguageWhenRequestLanguageIsMissing() {
		LocalWhisperAdapter adapter = new LocalWhisperAdapter(
			"whisper",
			"tiny",
			"Korean",
			false,
			Path.of("build/transcripts"),
			60
		);

		assertThat(adapter.buildCommand(
			new TranscriptionPort.TranscriptionRequest("memo-1", Path.of("recording.webm"), null),
			Path.of("build/transcripts/memo-1")
		)).containsSequence("--language", "Korean");
	}

	@Test
	void buildCommandSkipsBlankModelAndLanguage() {
		LocalWhisperAdapter adapter = new LocalWhisperAdapter(
			"whisper",
			" ",
			" ",
			false,
			Path.of("build/transcripts"),
			60
		);

		assertThat(adapter.buildCommand(
			new TranscriptionPort.TranscriptionRequest("memo-1", Path.of("recording.webm"), null),
			Path.of("build/transcripts/memo-1")
		)).doesNotContain("--model", "--language");
	}
}
