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
public class AudioFileImportService {

	private static final int MAX_DURATION_SECONDS = 7_200;

	private final UserAccountRepository userAccountRepository;
	private final VoiceMemoRepository voiceMemoRepository;
	private final AudioAssetRepository audioAssetRepository;
	private final StoragePort storagePort;
	private final TemporaryTitleGenerator titleGenerator;
	private final long maxUploadBytes;

	public AudioFileImportService(
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
	public ImportedAudioResult importAudio(ImportAudioCommand command) {
		validate(command);

		UserAccount owner = userAccountRepository.findById(command.ownerId())
			.orElseThrow(() -> new InvalidRecordingException("Authenticated user was not found."));

		String derivedTitle = sanitizeTitle(command.originalFilename());
		VoiceMemo memo = voiceMemoRepository.save(VoiceMemo.createUploaded(owner, derivedTitle));

		StoragePort.StoredObject storedObject = storagePort.store(new StoragePort.StoreObjectRequest(
			"memos/" + memo.getId(),
			extensionFor(command.contentType()),
			command.inputStream()
		));

		audioAssetRepository.save(AudioAsset.create(
			memo,
			storedObject.storageKey(),
			command.contentType(),
			command.sizeBytes(),
			normalizeFilename(command.originalFilename()),
			command.durationSeconds()
		));

		return ImportedAudioResult.from(memo);
	}

	private void validate(ImportAudioCommand command) {
		if (command.sizeBytes() <= 0) {
			throw new InvalidRecordingException("Audio file is empty.");
		}

		if (command.sizeBytes() > maxUploadBytes) {
			throw new InvalidRecordingException("Audio file is too large.");
		}

		String contentType = command.contentType();
		if (contentType == null) {
			throw new InvalidRecordingException("Audio file type is required.");
		}

		String lowerContentType = contentType.toLowerCase();
		if (!lowerContentType.startsWith("audio/webm") &&
			!lowerContentType.startsWith("audio/mpeg") &&
			!lowerContentType.startsWith("audio/mp3") &&
			!lowerContentType.startsWith("audio/wav") &&
			!lowerContentType.startsWith("audio/x-wav")) {
			throw new InvalidRecordingException("Only webm, mpeg, and wav audio files are supported.");
		}

		if (command.durationSeconds() != null) {
			if (command.durationSeconds() <= 0) {
				throw new InvalidRecordingException("Audio duration must be positive.");
			}
			if (command.durationSeconds() > MAX_DURATION_SECONDS) {
				throw new InvalidRecordingException("Audio duration cannot exceed 2 hours.");
			}
		}
	}

	private String sanitizeTitle(String filename) {
		String title = filename;
		if (title != null) {
			title = title.trim();
			int lastDotIndex = title.lastIndexOf('.');
			if (lastDotIndex >= 0) {
				title = title.substring(0, lastDotIndex);
			}
			title = title.replaceAll("[_\\-.]", " ");
			title = title.replaceAll("\\s+", " ");
			title = title.trim();
		}
		if (title == null || title.isEmpty()) {
			title = titleGenerator.generate();
		}
		return title;
	}

	private String extensionFor(String contentType) {
		String normalized = contentType.toLowerCase();
		if (normalized.startsWith("audio/webm")) {
			return "webm";
		}
		if (normalized.startsWith("audio/wav") || normalized.startsWith("audio/x-wav")) {
			return "wav";
		}
		return "mp3";
	}

	private String normalizeFilename(String filename) {
		if (filename == null) {
			return null;
		}

		String trimmed = filename.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	public record ImportAudioCommand(
		UUID ownerId,
		String originalFilename,
		String contentType,
		long sizeBytes,
		Integer durationSeconds,
		InputStream inputStream
	) {
		public ImportAudioCommand {
			if (inputStream == null) {
				throw new InvalidRecordingException("Audio file is required.");
			}
		}
	}

	public record ImportedAudioResult(
		String id,
		String title,
		String recordingStatus,
		String transcriptionStatus,
		String createdAt
	) {
		public static ImportedAudioResult from(VoiceMemo memo) {
			return new ImportedAudioResult(
				memo.getId() != null ? memo.getId().toString() : null,
				memo.getTitle(),
				memo.getRecordingStatus() != null ? memo.getRecordingStatus().name() : null,
				memo.getTranscriptionStatus() != null ? memo.getTranscriptionStatus().name() : null,
				memo.getCreatedAt() != null ? memo.getCreatedAt().toString() : null
			);
		}
	}
}
