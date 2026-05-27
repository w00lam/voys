package com.voys.notes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.voys.identity.infrastructure.persistence.UserAccount;
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

class GeneratedNoteServiceTests {

	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID MEMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final VoiceMemoRepository voiceMemoRepository = mock(VoiceMemoRepository.class);
	private final TranscriptRepository transcriptRepository = mock(TranscriptRepository.class);
	private final GeneratedNoteRepository generatedNoteRepository = mock(GeneratedNoteRepository.class);
	private final GeneratedNoteGenerator generator = mock(GeneratedNoteGenerator.class);
	private final GeneratedNoteService service = new GeneratedNoteService(
		voiceMemoRepository,
		transcriptRepository,
		generatedNoteRepository,
		generator
	);

	@Test
	void generateStoresDraftNoteForOwnedCompletedTranscript() {
		VoiceMemo memo = completedMemo();
		Transcript transcript = Transcript.create(memo, """
			The team reviewed launch strategy and risk owners.
			Key risk is unclear rollout sequencing.
			Follow up with Alex on mitigation owners.
			""");
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));
		when(transcriptRepository.findByMemoId(MEMO_ID)).thenReturn(Optional.of(transcript));
		when(generator.generate(transcript.getText())).thenReturn(new GeneratedNoteDraft(
			"The team reviewed launch strategy and risk owners.",
			List.of("Key risk is unclear rollout sequencing."),
			List.of("Follow up with Alex on mitigation owners.")
		));
		when(generatedNoteRepository.save(any(GeneratedNote.class))).thenAnswer(invocation -> invocation.getArgument(0));

		GeneratedNoteService.GeneratedNoteResponse response = service.generate(OWNER_ID, MEMO_ID);

		assertThat(response.status()).isEqualTo("GENERATED");
		assertThat(response.summary()).contains("launch strategy");
		assertThat(response.keyPoints()).containsExactly("Key risk is unclear rollout sequencing.");
		assertThat(response.actionItems()).containsExactly("Follow up with Alex on mitigation owners.");
		assertThat(transcript.getText()).contains("launch strategy");
	}

	@Test
	void generateRejectsMemoWithoutCompletedTranscriptBeforeSaving() {
		VoiceMemo memo = uploadedMemo();
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));
		when(transcriptRepository.findByMemoId(MEMO_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.generate(OWNER_ID, MEMO_ID))
			.isInstanceOf(GeneratedNoteNotReadyException.class)
			.hasMessageContaining("completed transcript");

		verifyNoInteractions(generatedNoteRepository, generator);
	}

	@Test
	void getGeneratedNoteUsesOwnerScopedLookup() {
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getGeneratedNote(OWNER_ID, MEMO_ID))
			.isInstanceOf(MemoNotFoundException.class);

		verifyNoInteractions(generatedNoteRepository, generator);
	}

	@Test
	void updateGeneratedNoteReplacesEditableFieldsWithoutChangingTranscript() {
		VoiceMemo memo = completedMemo();
		Transcript transcript = Transcript.create(memo, "Original transcript text.");
		GeneratedNote existing = new GeneratedNote(
			MEMO_ID,
			"GENERATED",
			"Original summary",
			List.of("Original point"),
			List.of("Original action"),
			null,
			null
		);
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));
		when(generatedNoteRepository.findById(MEMO_ID)).thenReturn(Optional.of(existing));
		when(transcriptRepository.findByMemoId(MEMO_ID)).thenReturn(Optional.of(transcript));
		when(generatedNoteRepository.save(any(GeneratedNote.class))).thenAnswer(invocation -> invocation.getArgument(0));

		GeneratedNoteService.GeneratedNoteResponse response = service.updateGeneratedNote(
			OWNER_ID,
			MEMO_ID,
			new GeneratedNoteService.UpdateGeneratedNoteCommand(
				"Edited summary",
				List.of("Edited point"),
				List.of("Edited action")
			)
		);

		assertThat(response.summary()).isEqualTo("Edited summary");
		assertThat(response.keyPoints()).containsExactly("Edited point");
		assertThat(response.actionItems()).containsExactly("Edited action");
		assertThat(transcript.getText()).isEqualTo("Original transcript text.");
	}

	@Test
	void updateGeneratedNoteRejectsBlankSummary() {
		VoiceMemo memo = completedMemo();
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));

		assertThatThrownBy(() -> service.updateGeneratedNote(
			OWNER_ID,
			MEMO_ID,
			new GeneratedNoteService.UpdateGeneratedNoteCommand("  ", List.of(), List.of())
		))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Summary");
	}

	@Test
	void exportGeneratedNoteReturnsReadablePlainText() {
		VoiceMemo memo = completedMemo();
		GeneratedNote existing = new GeneratedNote(
			MEMO_ID,
			"GENERATED",
			"Edited summary",
			List.of("Edited point"),
			List.of("Edited action"),
			null,
			null
		);
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));
		when(generatedNoteRepository.findById(MEMO_ID)).thenReturn(Optional.of(existing));

		String exported = service.exportGeneratedNote(OWNER_ID, MEMO_ID);

		assertThat(exported).contains("Summary", "Edited summary", "Key Points", "Edited point", "Action Items", "Edited action");
	}

	@Test
	void exportTranscriptReturnsRawTranscriptText() {
		VoiceMemo memo = completedMemo();
		Transcript transcript = Transcript.create(memo, "Original transcript text.");
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));
		when(transcriptRepository.findByMemoId(MEMO_ID)).thenReturn(Optional.of(transcript));

		assertThat(service.exportTranscript(OWNER_ID, MEMO_ID)).isEqualTo("Original transcript text.");
	}

	private VoiceMemo completedMemo() {
		VoiceMemo memo = uploadedMemo();
		memo.markTranscriptionCompleted();
		return memo;
	}

	private VoiceMemo uploadedMemo() {
		UserAccount owner = UserAccount.create("user@example.com", "Voys User", "hash");
		VoiceMemo memo = VoiceMemo.createUploaded(owner, "Launch strategy");
		ReflectionTestUtils.setField(memo, "id", MEMO_ID);
		return memo;
	}
}
