package com.voys.memo.application;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.identity.infrastructure.persistence.UserAccountRepository;
import com.voys.memo.domain.InvalidRecordingException;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;

@Service
public class RecordingUploadService {

	private static final int MAX_DURATION_SECONDS = 7_200;

	private final UserAccountRepository userAccountRepository;
	private final VoiceMemoRepository voiceMemoRepository;
	private final AudioAssetRepository audioAssetRepository;
	private final StoragePort storagePort;
	private final TemporaryTitleGenerator titleGenerator;
	private final long maxUploadBytes;

	public RecordingUploadService(
		UserAccountRepository userAccountRepository,
		VoiceMemoRepository voiceMemoRepository,
		AudioAssetRepository audioAssetRepository,
		StoragePort storagePort,
		TemporaryTitleGenerator titleGenerator,
		@Value("${voys.recording.max-upload-bytes}") long maxUploadBytes
	) {
		this.userAccountRepository = userAccountRepository;
		this.voiceMemoRepository = voiceMemoRepository;
		this.audioAssetRepository = audioAssetRepository;
		this.storagePort = storagePort;
		this.titleGenerator = titleGenerator;
		this.maxUploadBytes = maxUploadBytes;
	}

	@Transactional
	public RecordingUploadResult upload(RecordingUploadCommand command) {
		validate(command);

		UserAccount owner = userAccountRepository.findById(command.ownerId())
			.orElseThrow(() -> new InvalidRecordingException("Authenticated user was not found."));

		VoiceMemo memo = voiceMemoRepository.save(VoiceMemo.createUploaded(owner, titleGenerator.generate()));
		StoragePort.StoredObject storedObject = storagePort.store(new StoragePort.StoreObjectRequest(
			"memos/" + memo.getId(),
			"webm",
			command.inputStream()
		));

		audioAssetRepository.save(AudioAsset.create(
			memo,
			storedObject.storageKey(),
			command.contentType(),
			command.sizeBytes(),
			command.originalFilename(),
			command.durationSeconds()
		));

		return RecordingUploadResult.from(memo);
	}

	private void validate(RecordingUploadCommand command) {
		if (command.sizeBytes() <= 0) {
			throw new InvalidRecordingException("Recording file is empty.");
		}

		if (command.sizeBytes() > maxUploadBytes) {
			throw new InvalidRecordingException("Recording file is too large.");
		}

		String contentType = command.contentType();
		if (contentType == null || !contentType.toLowerCase().startsWith("audio/webm")) {
			throw new InvalidRecordingException("Only WebM audio recordings are supported.");
		}

		if (command.durationSeconds() != null) {
			if (command.durationSeconds() <= 0) {
				throw new InvalidRecordingException("Recording duration must be positive.");
			}
			if (command.durationSeconds() > MAX_DURATION_SECONDS) {
				throw new InvalidRecordingException("Recording duration cannot exceed 2 hours.");
			}
		}
	}

	public record RecordingUploadCommand(
		UUID ownerId,
		String originalFilename,
		String contentType,
		long sizeBytes,
		Integer durationSeconds,
		InputStream inputStream
	) {
		public RecordingUploadCommand {
			if (inputStream == null) {
				throw new InvalidRecordingException("Recording file is required.");
			}
		}
	}

	public record RecordingUploadResult(
		String id,
		String title,
		String recordingStatus,
		String transcriptionStatus,
		String createdAt
	) {
		static RecordingUploadResult from(VoiceMemo memo) {
			return new RecordingUploadResult(
				memo.getId().toString(),
				memo.getTitle(),
				memo.getRecordingStatus().name(),
				memo.getTranscriptionStatus().name(),
				memo.getCreatedAt().toString()
			);
		}
	}
}
