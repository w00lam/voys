package com.voys.transcription.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.memo.application.StoragePort;
import com.voys.memo.domain.TranscriptionStatus;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptRepository;
import com.voys.transcription.infrastructure.persistence.TranscriptSegmentRepository;

class TranscriptionWorkflowServiceSuggestedTitleTests {

	private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID memoId = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final VoiceMemoRepository voiceMemoRepository = mock(VoiceMemoRepository.class);
	private final AudioAssetRepository audioAssetRepository = mock(AudioAssetRepository.class);
	private final TranscriptRepository transcriptRepository = mock(TranscriptRepository.class);
	private final TranscriptSegmentRepository transcriptSegmentRepository = mock(TranscriptSegmentRepository.class);
	private final StoragePort storagePort = mock(StoragePort.class);
	private final FakeTranscriptionPort transcriptionPort = new FakeTranscriptionPort();
	private final RecordingTranscriptionJobRunner jobRunner = new RecordingTranscriptionJobRunner();

	private TranscriptionWorkflowService service;
	private VoiceMemo memo;
	private AudioAsset audio;

	@BeforeEach
	void setUp() {
		memo = memoWithId(memoId);
		audio = AudioAsset.create(memo, "memos/%s/audio.webm".formatted(memoId), "audio/webm;codecs=opus", 5L, "audio.webm", 3);

		when(voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)).thenReturn(Optional.of(memo));
		when(audioAssetRepository.findByMemoId(memoId)).thenReturn(Optional.of(audio));
		when(storagePort.get(audio.getStorageKey())).thenReturn(new StoragePort.StoredResource(
			new ByteArrayResource("audio".getBytes()),
			5L,
			Path.of("storage/audio/memos/%s/audio.webm".formatted(memoId))
		));
		when(transcriptRepository.findByMemoId(memoId)).thenReturn(Optional.empty());
		when(transcriptRepository.save(any(Transcript.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service = new TranscriptionWorkflowService(
			voiceMemoRepository,
			audioAssetRepository,
			transcriptRepository,
			transcriptSegmentRepository,
			storagePort,
			transcriptionPort,
			jobRunner,
			transactionTemplate()
		);
	}

	@Test
	void completedTranscriptionStoresSuggestedTitleWithoutChangingCurrentTitle() {
		transcriptionPort.completeWith("Product strategy sync. The team discussed launch risks and roadmap.");

		service.startTranscription(ownerId, memoId);
		jobRunner.runOnlyJob();

		assertThat(memo.getTranscriptionStatus()).isEqualTo(TranscriptionStatus.COMPLETED);
		assertThat(memo.getTitle()).isEqualTo("Recording 2026-05-27 13:15");
		assertThat(memo.getSuggestedTitle()).isEqualTo("Product strategy sync");
	}

	@Test
	void transcriptResponseIncludesSuggestedTitle() {
		Transcript transcript = Transcript.create(memo, "Product strategy sync. The team discussed launch risks.");
		memo.setSuggestedTitle("Product strategy sync");
		when(transcriptRepository.findByMemoId(memoId)).thenReturn(Optional.of(transcript));

		var response = service.getTranscript(ownerId, memoId);

		assertThat(response.suggestedTitle()).isEqualTo("Product strategy sync");
	}

	private VoiceMemo memoWithId(UUID id) {
		VoiceMemo voiceMemo = VoiceMemo.createUploaded(mock(UserAccount.class), "Recording 2026-05-27 13:15");
		ReflectionTestUtils.setField(voiceMemo, "id", id);
		return voiceMemo;
	}

	private static TransactionTemplate transactionTemplate() {
		return new TransactionTemplate(new PlatformTransactionManager() {
			@Override
			public TransactionStatus getTransaction(TransactionDefinition definition) {
				return new SimpleTransactionStatus();
			}

			@Override
			public void commit(TransactionStatus status) {
			}

			@Override
			public void rollback(TransactionStatus status) {
			}
		});
	}

	private static final class RecordingTranscriptionJobRunner implements TranscriptionJobRunner {

		private final List<Runnable> queuedJobs = new ArrayList<>();

		@Override
		public void submit(Runnable job) {
			queuedJobs.add(job);
		}

		void runOnlyJob() {
			assertThat(queuedJobs).hasSize(1);
			queuedJobs.get(0).run();
		}
	}

	private static final class FakeTranscriptionPort implements TranscriptionPort {

		private String text = "transcribed text";

		@Override
		public TranscriptionResult transcribe(TranscriptionRequest request) {
			return new TranscriptionResult(text);
		}

		void completeWith(String text) {
			this.text = text;
		}
	}
}
