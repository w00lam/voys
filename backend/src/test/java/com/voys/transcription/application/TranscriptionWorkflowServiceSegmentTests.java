package com.voys.transcription.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
import com.voys.transcription.domain.TranscriptionFailedException;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptRepository;
import com.voys.transcription.infrastructure.persistence.TranscriptSegment;
import com.voys.transcription.infrastructure.persistence.TranscriptSegmentRepository;

class TranscriptionWorkflowServiceSegmentTests {

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
	void queuedJobStoresTimestampedSegmentsInWhisperOrderWhenWhisperSucceeds() {
		transcriptionPort.completeWith(new TranscriptionPort.TranscriptionResult(
			"intro roadmap",
			List.of(
				new TranscriptionPort.TranscriptionSegment(0.0, 4.2, "intro"),
				new TranscriptionPort.TranscriptionSegment(4.2, 8.0, "roadmap")
			)
		));

		service.startTranscription(ownerId, memoId);
		jobRunner.runOnlyJob();

		ArgumentCaptor<Transcript> transcriptCaptor = ArgumentCaptor.forClass(Transcript.class);
		verify(transcriptRepository).save(transcriptCaptor.capture());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Iterable<TranscriptSegment>> segmentCaptor = ArgumentCaptor.forClass(Iterable.class);
		verify(transcriptSegmentRepository).saveAll(segmentCaptor.capture());

		List<TranscriptSegment> segments = StreamSupport.stream(segmentCaptor.getValue().spliterator(), false).toList();

		assertThat(memo.getTranscriptionStatus()).isEqualTo(TranscriptionStatus.COMPLETED);
		assertThat(segments).hasSize(2);
		assertThat(segments.get(0).getTranscript()).isSameAs(transcriptCaptor.getValue());
		assertThat(segments.get(0).getPosition()).isZero();
		assertThat(segments.get(0).getStartSeconds()).isEqualTo(0.0);
		assertThat(segments.get(0).getEndSeconds()).isEqualTo(4.2);
		assertThat(segments.get(0).getText()).isEqualTo("intro");
		assertThat(segments.get(1).getTranscript()).isSameAs(transcriptCaptor.getValue());
		assertThat(segments.get(1).getPosition()).isEqualTo(1);
		assertThat(segments.get(1).getStartSeconds()).isEqualTo(4.2);
		assertThat(segments.get(1).getEndSeconds()).isEqualTo(8.0);
		assertThat(segments.get(1).getText()).isEqualTo("roadmap");
	}

	@Test
	void queuedJobDeletesExistingSegmentsBeforeSavingRegeneratedSegments() {
		Transcript existingTranscript = Transcript.create(memo, "old text");
		when(transcriptRepository.findByMemoId(memoId)).thenReturn(Optional.of(existingTranscript));
		transcriptionPort.completeWith(new TranscriptionPort.TranscriptionResult(
			"new text",
			List.of(new TranscriptionPort.TranscriptionSegment(1.0, 2.5, "new segment"))
		));

		service.startTranscription(ownerId, memoId);
		jobRunner.runOnlyJob();

		InOrder persistenceOrder = inOrder(transcriptSegmentRepository, transcriptRepository);
		persistenceOrder.verify(transcriptSegmentRepository).deleteByTranscript(existingTranscript);
		persistenceOrder.verify(transcriptRepository).save(existingTranscript);
		persistenceOrder.verify(transcriptSegmentRepository).saveAll(any());
		assertThat(existingTranscript.getText()).isEqualTo("new text");
	}

	@Test
	void queuedJobDoesNotSaveSegmentsWhenWhisperFails() {
		transcriptionPort.failWith(new TranscriptionFailedException("test failure"));

		service.startTranscription(ownerId, memoId);
		jobRunner.runOnlyJob();

		assertThat(memo.getTranscriptionStatus()).isEqualTo(TranscriptionStatus.FAILED);
		verify(transcriptSegmentRepository, never()).deleteByTranscript(any());
		verify(transcriptSegmentRepository, never()).saveAll(any());
	}

	private VoiceMemo memoWithId(UUID id) {
		VoiceMemo voiceMemo = VoiceMemo.createUploaded(mock(UserAccount.class), "Recording 2026-05-22 11:00");
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

		private TranscriptionPort.TranscriptionResult result = new TranscriptionPort.TranscriptionResult(
			"transcribed text",
			List.of(new TranscriptionPort.TranscriptionSegment(0.0, 1.0, "transcribed text"))
		);
		private RuntimeException failure;

		@Override
		public TranscriptionPort.TranscriptionResult transcribe(TranscriptionPort.TranscriptionRequest request) {
			if (failure != null) {
				throw failure;
			}
			return result;
		}

		void completeWith(TranscriptionPort.TranscriptionResult result) {
			this.result = result;
		}

		void failWith(RuntimeException exception) {
			failure = exception;
		}
	}
}
