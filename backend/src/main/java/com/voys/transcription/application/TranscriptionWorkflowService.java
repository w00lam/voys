package com.voys.transcription.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.voys.memo.application.StoragePort;
import com.voys.memo.domain.MemoNotFoundException;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;
import com.voys.transcription.domain.TranscriptionFailedException;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptRepository;

@Service
public class TranscriptionWorkflowService {

	private final VoiceMemoRepository voiceMemoRepository;
	private final AudioAssetRepository audioAssetRepository;
	private final TranscriptRepository transcriptRepository;
	private final StoragePort storagePort;
	private final TranscriptionPort transcriptionPort;
	private final TransactionTemplate transactionTemplate;

	public TranscriptionWorkflowService(
		VoiceMemoRepository voiceMemoRepository,
		AudioAssetRepository audioAssetRepository,
		TranscriptRepository transcriptRepository,
		StoragePort storagePort,
		TranscriptionPort transcriptionPort,
		TransactionTemplate transactionTemplate
	) {
		this.voiceMemoRepository = voiceMemoRepository;
		this.audioAssetRepository = audioAssetRepository;
		this.transcriptRepository = transcriptRepository;
		this.storagePort = storagePort;
		this.transcriptionPort = transcriptionPort;
		this.transactionTemplate = transactionTemplate;
	}

	public TranscriptionResponse startTranscription(UUID ownerId, UUID memoId) {
		AudioAsset audio = transactionTemplate.execute(status -> {
			VoiceMemo memo = findOwnedMemo(ownerId, memoId);
			memo.markTranscriptionProcessing();
			return findAudio(memoId);
		});

		try {
			StoragePort.StoredResource stored = storagePort.get(audio.getStorageKey());
			TranscriptionPort.TranscriptionResult result = transcriptionPort.transcribe(
				new TranscriptionPort.TranscriptionRequest(memoId.toString(), stored.localPath(), null)
			);

			return transactionTemplate.execute(status -> {
				VoiceMemo memo = findOwnedMemo(ownerId, memoId);
				Transcript transcript = transcriptRepository.findByMemoId(memoId)
					.map(existing -> {
						existing.replaceText(result.text());
						return existing;
					})
					.orElseGet(() -> Transcript.create(memo, result.text()));
				transcriptRepository.save(transcript);
				memo.markTranscriptionCompleted();
				return TranscriptionResponse.from(memo, transcript);
			});
		} catch (RuntimeException exception) {
			transactionTemplate.executeWithoutResult(status -> {
				VoiceMemo memo = findOwnedMemo(ownerId, memoId);
				memo.markTranscriptionFailed();
			});

			if (exception instanceof TranscriptionFailedException failedException) {
				throw failedException;
			}

			throw new TranscriptionFailedException("Transcription failed.", exception);
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
				transcript == null ? null : transcript.getUpdatedAt().toString()
			);
		}
	}
}
