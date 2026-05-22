package com.voys.transcription.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.voys.memo.application.StoragePort;
import com.voys.memo.domain.MemoNotFoundException;
import com.voys.memo.domain.TranscriptionStatus;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;
import com.voys.transcription.domain.TranscriptionAlreadyRunningException;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptRepository;
import com.voys.transcription.infrastructure.persistence.TranscriptSegment;
import com.voys.transcription.infrastructure.persistence.TranscriptSegmentRepository;

@Service
public class TranscriptionWorkflowService {

	private final VoiceMemoRepository voiceMemoRepository;
	private final AudioAssetRepository audioAssetRepository;
	private final TranscriptRepository transcriptRepository;
	private final TranscriptSegmentRepository transcriptSegmentRepository;
	private final StoragePort storagePort;
	private final TranscriptionPort transcriptionPort;
	private final TranscriptionJobRunner transcriptionJobRunner;
	private final TransactionTemplate transactionTemplate;

	public TranscriptionWorkflowService(
		VoiceMemoRepository voiceMemoRepository,
		AudioAssetRepository audioAssetRepository,
		TranscriptRepository transcriptRepository,
		TranscriptSegmentRepository transcriptSegmentRepository,
		StoragePort storagePort,
		TranscriptionPort transcriptionPort,
		TranscriptionJobRunner transcriptionJobRunner,
		TransactionTemplate transactionTemplate
	) {
		this.voiceMemoRepository = voiceMemoRepository;
		this.audioAssetRepository = audioAssetRepository;
		this.transcriptRepository = transcriptRepository;
		this.transcriptSegmentRepository = transcriptSegmentRepository;
		this.storagePort = storagePort;
		this.transcriptionPort = transcriptionPort;
		this.transcriptionJobRunner = transcriptionJobRunner;
		this.transactionTemplate = transactionTemplate;
	}

	public TranscriptionResponse startTranscription(UUID ownerId, UUID memoId) {
		AudioAsset audio = transactionTemplate.execute(status -> {
			VoiceMemo memo = findOwnedMemo(ownerId, memoId);
			if (memo.getTranscriptionStatus() == TranscriptionStatus.PROCESSING) {
				throw new TranscriptionAlreadyRunningException(memoId);
			}
			memo.markTranscriptionProcessing();
			return findAudio(memoId);
		});

		transcriptionJobRunner.submit(() -> runTranscriptionJob(ownerId, memoId, audio));

		return transactionTemplate.execute(status -> {
			VoiceMemo memo = findOwnedMemo(ownerId, memoId);
			Transcript transcript = transcriptRepository.findByMemoId(memoId).orElse(null);
			return TranscriptionResponse.from(memo, transcript);
		});
	}

	private void runTranscriptionJob(UUID ownerId, UUID memoId, AudioAsset audio) {
		try {
			StoragePort.StoredResource stored = storagePort.get(audio.getStorageKey());
			TranscriptionPort.TranscriptionResult result = transcriptionPort.transcribe(
				new TranscriptionPort.TranscriptionRequest(memoId.toString(), stored.localPath(), null)
			);

			transactionTemplate.executeWithoutResult(status -> {
				VoiceMemo memo = findOwnedMemo(ownerId, memoId);
				Transcript transcript = transcriptRepository.findByMemoId(memoId)
					.map(existing -> {
						existing.replaceText(result.text());
						transcriptSegmentRepository.deleteByTranscript(existing);
						return existing;
					})
					.orElseGet(() -> Transcript.create(memo, result.text()));

				Transcript savedTranscript = transcriptRepository.save(transcript);

				if (result.segments() != null) {
					List<TranscriptSegment> segmentsToSave = new ArrayList<>();
					for (int i = 0; i < result.segments().size(); i++) {
						var segResult = result.segments().get(i);
						segmentsToSave.add(TranscriptSegment.create(
							savedTranscript,
							i,
							segResult.startSeconds(),
							segResult.endSeconds(),
							segResult.text()
						));
					}
					transcriptSegmentRepository.saveAll(segmentsToSave);
				}

				memo.markTranscriptionCompleted();
			});
		} catch (RuntimeException exception) {
			transactionTemplate.executeWithoutResult(status -> {
				VoiceMemo memo = findOwnedMemo(ownerId, memoId);
				memo.markTranscriptionFailed();
			});
		}
	}

	public TranscriptionResponse getTranscript(UUID ownerId, UUID memoId) {
		return transactionTemplate.execute(status -> {
			VoiceMemo memo = findOwnedMemo(ownerId, memoId);
			Transcript transcript = transcriptRepository.findByMemoId(memoId).orElse(null);
			return TranscriptionResponse.from(memo, transcript);
		});
	}

	private VoiceMemo findOwnedMemo(UUID ownerId, UUID memoId) {
		return voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));
	}

	private AudioAsset findAudio(UUID memoId) {
		return audioAssetRepository.findByMemoId(memoId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));
	}

	public record TranscriptionResponse(
		String memoId,
		String status,
		String text,
		String updatedAt
	) {
		static TranscriptionResponse from(VoiceMemo memo, Transcript transcript) {
			return new TranscriptionResponse(
				memo.getId().toString(),
				memo.getTranscriptionStatus().name(),
				transcript == null ? null : transcript.getText(),
				(transcript == null || transcript.getUpdatedAt() == null) ? null : transcript.getUpdatedAt().toString()
			);
		}
	}
}
