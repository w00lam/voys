package com.voys.transcription.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger log = LoggerFactory.getLogger(TranscriptionWorkflowService.class);

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
			List<TranscriptSegment> segments = List.of();
			if (transcript != null) {
				segments = transcriptSegmentRepository.findByTranscriptOrderByPositionAsc(transcript);
			}
			return TranscriptionResponse.from(memo, transcript, segments);
		});
	}

	private record FailureReason(String code, String message, boolean retryable) {}

	private FailureReason mapToFailureReason(Throwable exception) {
		String msg = exception.getMessage() != null ? exception.getMessage() : "";
		Throwable cause = exception.getCause();
		String causeMsg = cause != null && cause.getMessage() != null ? cause.getMessage() : "";

		String fullText = (msg + " " + causeMsg).toLowerCase();

		if (msg.contains("Whisper CLI could not be executed.")) {
			return new FailureReason(
				"WHISPER_COMMAND_NOT_FOUND",
				"Whisper CLI is not installed or not available to the backend process.",
				true
			);
		} else if (msg.contains("Whisper transcription timed out.")) {
			return new FailureReason(
				"WHISPER_TIMEOUT",
				"Whisper transcription timed out.",
				true
			);
		} else if (msg.contains("Whisper did not produce a JSON transcript.")) {
			return new FailureReason(
				"WHISPER_EMPTY_OUTPUT",
				"Whisper completed but did not produce usable transcript output.",
				true
			);
		} else if (fullText.contains("invalid audio") || fullText.contains("decode error") ||
				fullText.contains("ffmpeg") || fullText.contains("error decoding") ||
				fullText.contains("unsupported") || fullText.contains("invalid")) {
			return new FailureReason(
				"AUDIO_UNSUPPORTED_OR_INVALID",
				"Audio cannot be processed by Whisper or ffmpeg.",
				false
			);
		} else {
			return new FailureReason(
				"TRANSCRIPTION_UNEXPECTED_ERROR",
				"An unexpected error occurred during transcription.",
				true
			);
		}
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
			log.error("Transcription job failed for memo {}", memoId, exception);
			transactionTemplate.executeWithoutResult(status -> {
				VoiceMemo memo = findOwnedMemo(ownerId, memoId);
				FailureReason mapped = mapToFailureReason(exception);
				memo.markTranscriptionFailed(mapped.code(), mapped.message(), mapped.retryable());
			});
		}
	}

	public TranscriptionResponse getTranscript(UUID ownerId, UUID memoId) {
		return transactionTemplate.execute(status -> {
			VoiceMemo memo = findOwnedMemo(ownerId, memoId);
			Transcript transcript = transcriptRepository.findByMemoId(memoId).orElse(null);
			List<TranscriptSegment> segments = List.of();
			if (transcript != null) {
				segments = transcriptSegmentRepository.findByTranscriptOrderByPositionAsc(transcript);
			}
			return TranscriptionResponse.from(memo, transcript, segments);
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

	public record SegmentDto(
		int position,
		double startSeconds,
		double endSeconds,
		String text
	) {}

	public record FailureReasonDto(
		String code,
		String message,
		boolean retryable
	) {}

	public record TranscriptionResponse(
		String memoId,
		String status,
		String text,
		List<SegmentDto> segments,
		FailureReasonDto failureReason,
		String updatedAt
	) {
		static TranscriptionResponse from(VoiceMemo memo, Transcript transcript, List<TranscriptSegment> segments) {
			List<SegmentDto> segmentDtos = new ArrayList<>();
			if (segments != null) {
				for (var segment : segments) {
					segmentDtos.add(new SegmentDto(
						segment.getPosition(),
						segment.getStartSeconds(),
						segment.getEndSeconds(),
						segment.getText()
					));
				}
			}
			FailureReasonDto failureReasonDto = null;
			if (memo.getTranscriptionStatus() == TranscriptionStatus.FAILED && memo.getFailureReasonCode() != null) {
				failureReasonDto = new FailureReasonDto(
					memo.getFailureReasonCode(),
					memo.getFailureReasonMessage(),
					Boolean.TRUE.equals(memo.getFailureReasonRetryable())
				);
			}
			return new TranscriptionResponse(
				memo.getId().toString(),
				memo.getTranscriptionStatus().name(),
				transcript == null ? null : transcript.getText(),
				segmentDtos,
				failureReasonDto,
				(transcript == null || transcript.getUpdatedAt() == null) ? null : transcript.getUpdatedAt().toString()
			);
		}
	}
}
