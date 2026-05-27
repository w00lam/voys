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
