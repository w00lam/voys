package com.voys.transcription.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.voys.memo.domain.MemoNotFoundException;
import com.voys.memo.domain.TranscriptionStatus;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;
import com.voys.transcription.domain.TranscriptionAlreadyRunningException;
import com.voys.transcription.domain.TranscriptionFailedException;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptRepository;
import com.voys.transcription.infrastructure.persistence.TranscriptSegmentRepository;

class TranscriptionWorkflowServiceBackgroundTests {

	private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID otherOwnerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
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
		when(voiceMemoRepository.findByIdAndOwnerId(memoId, otherOwnerId)).thenReturn(Optional.empty());
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
	void startTranscriptionMarksProcessingAndQueuesBackgroundJobWithoutRunningWhisperInline() {
		var response = service.startTranscription(ownerId, memoId);

		assertThat(response.memoId()).isEqualTo(memoId.toString());
		assertThat(response.status()).isEqualTo(TranscriptionStatus.PROCESSING.name());
		assertThat(memo.getTranscriptionStatus()).isEqualTo(TranscriptionStatus.PROCESSING);
		assertThat(jobRunner.queuedJobs()).hasSize(1);
		assertThat(transcriptionPort.invocations()).isZero();
		verify(transcriptRepository, never()).save(any(Transcript.class));
	}

	@Test
	void queuedJobStoresTranscriptAndMarksCompletedWhenWhisperSucceeds() {
		service.startTranscription(ownerId, memoId);

		jobRunner.runOnlyJob();

		assertThat(transcriptionPort.invocations()).isEqualTo(1);
		assertThat(memo.getTranscriptionStatus()).isEqualTo(TranscriptionStatus.COMPLETED);
		verify(transcriptRepository).save(any(Transcript.class));
	}

	@Test
	void queuedJobMarksFailedAndDoesNotSaveTranscriptWhenWhisperFails() {
		transcriptionPort.failWith(new TranscriptionFailedException("test failure"));

		service.startTranscription(ownerId, memoId);
		jobRunner.runOnlyJob();

		assertThat(memo.getTranscriptionStatus()).isEqualTo(TranscriptionStatus.FAILED);
		verify(transcriptRepository, never()).save(any(Transcript.class));
	}

	@Test
	void startTranscriptionRejectsMemoOwnedByAnotherUserWithoutQueuingJob() {
		assertThatThrownBy(() -> service.startTranscription(otherOwnerId, memoId))
			.isInstanceOf(MemoNotFoundException.class);

		assertThat(jobRunner.queuedJobs()).isEmpty();
		assertThat(transcriptionPort.invocations()).isZero();
	}

	@Test
	void startTranscriptionRejectsDuplicateStartWhenMemoIsAlreadyProcessing() {
		memo.markTranscriptionProcessing();

		assertThatThrownBy(() -> service.startTranscription(ownerId, memoId))
			.isInstanceOf(TranscriptionAlreadyRunningException.class);

		assertThat(jobRunner.queuedJobs()).isEmpty();
		assertThat(transcriptionPort.invocations()).isZero();
	}

	private VoiceMemo memoWithId(UUID id) {
		VoiceMemo voiceMemo = VoiceMemo.createUploaded(mock(UserAccount.class), "Recording 2026-05-21 16:01");
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

		List<Runnable> queuedJobs() {
			return queuedJobs;
		}

		void runOnlyJob() {
			assertThat(queuedJobs).hasSize(1);
			queuedJobs.get(0).run();
		}
	}

	private static final class FakeTranscriptionPort implements TranscriptionPort {

		private int invocations;
		private RuntimeException failure;

		@Override
		public TranscriptionResult transcribe(TranscriptionRequest request) {
			invocations++;
			if (failure != null) {
				throw failure;
			}
			return new TranscriptionResult("transcribed text");
		}

		void failWith(RuntimeException exception) {
			failure = exception;
		}

		int invocations() {
			return invocations;
		}
	}
}
