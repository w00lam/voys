package com.voys.transcription.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.memo.application.StoragePort;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptRepository;
import com.voys.transcription.infrastructure.persistence.TranscriptSegment;
import com.voys.transcription.infrastructure.persistence.TranscriptSegmentRepository;

class TranscriptionWorkflowServiceSegmentReadTests {

	private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID memoId = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final VoiceMemoRepository voiceMemoRepository = mock(VoiceMemoRepository.class);
	private final AudioAssetRepository audioAssetRepository = mock(AudioAssetRepository.class);
	private final TranscriptRepository transcriptRepository = mock(TranscriptRepository.class);
	private final TranscriptSegmentRepository transcriptSegmentRepository = mock(TranscriptSegmentRepository.class);
	private final StoragePort storagePort = mock(StoragePort.class);
	private final TranscriptionPort transcriptionPort = mock(TranscriptionPort.class);
	private final TranscriptionJobRunner transcriptionJobRunner = mock(TranscriptionJobRunner.class);

	private TranscriptionWorkflowService service;
	private VoiceMemo memo;

	@BeforeEach
	void setUp() {
		memo = memoWithId(memoId);
		when(voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)).thenReturn(Optional.of(memo));

		service = new TranscriptionWorkflowService(
			voiceMemoRepository,
			audioAssetRepository,
			transcriptRepository,
			transcriptSegmentRepository,
			storagePort,
			transcriptionPort,
			transcriptionJobRunner,
			transactionTemplate()
		);
	}

	@Test
	void getTranscriptReturnsSegmentsOrderedByPosition() {
		Transcript transcript = Transcript.create(memo, "intro roadmap");
		TranscriptSegment second = TranscriptSegment.create(transcript, 1, 4.2, 8.0, "roadmap");
		TranscriptSegment first = TranscriptSegment.create(transcript, 0, 0.0, 4.2, "intro");
		when(transcriptRepository.findByMemoId(memoId)).thenReturn(Optional.of(transcript));
		when(transcriptSegmentRepository.findByTranscriptOrderByPositionAsc(transcript)).thenReturn(List.of(first, second));

		var response = service.getTranscript(ownerId, memoId);

		assertThat(response.memoId()).isEqualTo(memoId.toString());
		assertThat(response.text()).isEqualTo("intro roadmap");
		assertThat(response.segments()).hasSize(2);
		assertThat(response.segments().get(0).position()).isZero();
		assertThat(response.segments().get(0).startSeconds()).isEqualTo(0.0);
		assertThat(response.segments().get(0).endSeconds()).isEqualTo(4.2);
		assertThat(response.segments().get(0).text()).isEqualTo("intro");
		assertThat(response.segments().get(1).position()).isEqualTo(1);
		assertThat(response.segments().get(1).startSeconds()).isEqualTo(4.2);
		assertThat(response.segments().get(1).endSeconds()).isEqualTo(8.0);
		assertThat(response.segments().get(1).text()).isEqualTo("roadmap");
	}

	@Test
	void getTranscriptReturnsEmptySegmentsWhenTranscriptDoesNotExist() {
		when(transcriptRepository.findByMemoId(memoId)).thenReturn(Optional.empty());

		var response = service.getTranscript(ownerId, memoId);

		assertThat(response.text()).isNull();
		assertThat(response.segments()).isEmpty();
	}

	private VoiceMemo memoWithId(UUID id) {
		VoiceMemo voiceMemo = VoiceMemo.createUploaded(mock(UserAccount.class), "Recording 2026-05-22 15:20");
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
}
