package com.voys.notes.application;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voys.memo.domain.MemoNotFoundException;
import com.voys.memo.domain.TranscriptionStatus;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;
import com.voys.notes.domain.GeneratedNote;
import com.voys.notes.domain.GeneratedNoteDraft;
import com.voys.notes.domain.GeneratedNoteNotReadyException;
import com.voys.notes.infrastructure.persistence.GeneratedNoteRepository;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptRepository;

@Service
public class GeneratedNoteService {

	private final VoiceMemoRepository voiceMemoRepository;
	private final TranscriptRepository transcriptRepository;
	private final GeneratedNoteRepository generatedNoteRepository;
	private final GeneratedNoteGenerator generator;

	public GeneratedNoteService(
		VoiceMemoRepository voiceMemoRepository,
		TranscriptRepository transcriptRepository,
		GeneratedNoteRepository generatedNoteRepository,
		GeneratedNoteGenerator generator
	) {
		this.voiceMemoRepository = voiceMemoRepository;
		this.transcriptRepository = transcriptRepository;
		this.generatedNoteRepository = generatedNoteRepository;
		this.generator = generator;
	}

	@Transactional
	public GeneratedNoteResponse generate(UUID ownerId, UUID memoId) {
		VoiceMemo memo = voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));

		if (memo.getTranscriptionStatus() != TranscriptionStatus.COMPLETED) {
			throw new GeneratedNoteNotReadyException(memoId);
		}

		Transcript transcript = transcriptRepository.findByMemoId(memoId)
			.orElseThrow(() -> new GeneratedNoteNotReadyException(memoId));

		GeneratedNoteDraft draft = generator.generate(transcript.getText());

		GeneratedNote note = new GeneratedNote(
			memoId,
			"GENERATED",
			draft.summary(),
			draft.keyPoints(),
			draft.actionItems(),
			null,
			Instant.now()
		);

		GeneratedNote saved = generatedNoteRepository.save(note);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public GeneratedNoteResponse getGeneratedNote(UUID ownerId, UUID memoId) {
		voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));

		return generatedNoteRepository.findById(memoId)
			.map(this::toResponse)
			.orElseGet(() -> defaultResponse(memoId));
	}

	@Transactional
	public GeneratedNoteResponse updateGeneratedNote(UUID ownerId, UUID memoId, UpdateGeneratedNoteCommand command) {
		if (command.summary() == null || command.summary().trim().isEmpty()) {
			throw new IllegalArgumentException("Summary cannot be blank.");
		}

		voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));

		GeneratedNote existing = generatedNoteRepository.findById(memoId)
			.orElseThrow(() -> new GeneratedNoteNotReadyException(memoId));

		existing.update(
			command.summary().trim(),
			normalizeLineList(command.keyPoints()),
			normalizeLineList(command.actionItems())
		);
		GeneratedNote saved = generatedNoteRepository.save(existing);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public String exportGeneratedNote(UUID ownerId, UUID memoId) {
		voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));

		GeneratedNote note = generatedNoteRepository.findById(memoId)
			.orElseThrow(() -> new GeneratedNoteNotReadyException(memoId));

		StringBuilder sb = new StringBuilder();
		sb.append("Summary\n");
		if (note.getSummary() != null) {
			sb.append(note.getSummary()).append("\n");
		}
		sb.append("\nKey Points\n");
		if (note.getKeyPoints() != null) {
			for (String point : note.getKeyPoints()) {
				sb.append("- ").append(point).append("\n");
			}
		}
		sb.append("\nAction Items\n");
		if (note.getActionItems() != null) {
			for (String item : note.getActionItems()) {
				sb.append("- ").append(item).append("\n");
			}
		}
		return sb.toString();
	}

	@Transactional(readOnly = true)
	public String exportTranscript(UUID ownerId, UUID memoId) {
		voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));

		Transcript transcript = transcriptRepository.findByMemoId(memoId)
			.orElseThrow(() -> new GeneratedNoteNotReadyException(memoId));

		return transcript.getText();
	}

	public record UpdateGeneratedNoteCommand(
		String summary,
		List<String> keyPoints,
		List<String> actionItems
	) {}

	private List<String> normalizeLineList(List<String> values) {
		if (values == null) {
			return List.of();
		}

		return values.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(value -> !value.isEmpty())
			.toList();
	}

	private GeneratedNoteResponse toResponse(GeneratedNote note) {
		return new GeneratedNoteResponse(
			note.getMemoId().toString(),
			note.getStatus(),
			note.getSummary(),
			note.getKeyPoints(),
			note.getActionItems(),
			note.getFailureReason(),
			note.getUpdatedAt() != null ? note.getUpdatedAt().toString() : null
		);
	}

	private GeneratedNoteResponse defaultResponse(UUID memoId) {
		return new GeneratedNoteResponse(
			memoId.toString(),
			"NOT_GENERATED",
			null,
			List.of(),
			List.of(),
			null,
			null
		);
	}

	public record GeneratedNoteResponse(
		String memoId,
		String status,
		String summary,
		List<String> keyPoints,
		List<String> actionItems,
		String failureReason,
		String updatedAt
	) {}
}
