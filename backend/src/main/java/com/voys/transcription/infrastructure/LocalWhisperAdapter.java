package com.voys.transcription.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voys.transcription.application.TranscriptionPort;
import com.voys.transcription.domain.TranscriptionFailedException;

@Component
public class LocalWhisperAdapter implements TranscriptionPort {

	private final String whisperCommand;
	private final Path outputRoot;
	private final Duration timeout;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public LocalWhisperAdapter(
		@Value("${voys.whisper.command:whisper}") String whisperCommand,
		@Value("${voys.whisper.output-root:storage/transcripts}") Path outputRoot,
		@Value("${voys.whisper.timeout-seconds:7200}") long timeoutSeconds
	) {
		this.whisperCommand = whisperCommand;
		this.outputRoot = outputRoot.toAbsolutePath().normalize();
		this.timeout = Duration.ofSeconds(timeoutSeconds);
	}

	@Override
	public TranscriptionResult transcribe(TranscriptionRequest request) {
		Path outputDirectory = outputRoot.resolve(request.memoId() + "-" + UUID.randomUUID()).normalize();
		try {
			Files.createDirectories(outputDirectory);
			List<String> command = buildCommand(request, outputDirectory);
			Process process = new ProcessBuilder(command)
				.redirectErrorStream(true)
				.start();

			boolean finished = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				throw new TranscriptionFailedException("Whisper transcription timed out.");
			}

			if (process.exitValue() != 0) {
				String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
				throw new TranscriptionFailedException("Whisper transcription failed: " + output);
			}

			return readTranscriptJson(outputDirectory);
		} catch (IOException exception) {
			throw new TranscriptionFailedException("Whisper CLI could not be executed.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new TranscriptionFailedException("Whisper transcription was interrupted.", exception);
		}
	}

	private List<String> buildCommand(TranscriptionRequest request, Path outputDirectory) {
		List<String> command = new ArrayList<>();
		command.add(whisperCommand);
		command.add(request.audioPath().toString());
		command.add("--output_dir");
		command.add(outputDirectory.toString());
		command.add("--output_format");
		command.add("json");

		if (request.language() != null && !request.language().isBlank()) {
			command.add("--language");
			command.add(request.language());
		}

		return command;
	}

	private TranscriptionResult readTranscriptJson(Path outputDirectory) throws IOException {
		try (var paths = Files.list(outputDirectory)) {
			Path jsonPath = paths
				.filter(path -> path.getFileName().toString().endsWith(".json"))
				.findFirst()
				.orElseThrow(() -> new TranscriptionFailedException("Whisper did not produce a JSON transcript."));

			byte[] jsonData = Files.readAllBytes(jsonPath);
			JsonNode root = objectMapper.readTree(jsonData);
			String text = root.path("text").asText("").trim();

			List<TranscriptionSegment> segments = new ArrayList<>();
			JsonNode segmentsNode = root.path("segments");
			if (segmentsNode.isArray()) {
				for (JsonNode segmentNode : segmentsNode) {
					double start = segmentNode.path("start").asDouble();
					double end = segmentNode.path("end").asDouble();
					String segmentText = segmentNode.path("text").asText("").trim();
					segments.add(new TranscriptionSegment(start, end, segmentText));
				}
			}

			return new TranscriptionResult(text, segments);
		}
	}
}

