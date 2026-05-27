package com.voys.memo.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voys.memo.domain.InvalidMemoFolderException;
import com.voys.memo.domain.InvalidMemoTitleException;
import com.voys.memo.domain.MemoNotFoundException;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;

@Service
public class MemoLibraryService {

	private final VoiceMemoRepository voiceMemoRepository;
	private final AudioAssetRepository audioAssetRepository;
	private final StoragePort storagePort;

	public MemoLibraryService(
		VoiceMemoRepository voiceMemoRepository,
		AudioAssetRepository audioAssetRepository,
		StoragePort storagePort
	) {
		this.voiceMemoRepository = voiceMemoRepository;
		this.audioAssetRepository = audioAssetRepository;
		this.storagePort = storagePort;
	}

	@Transactional(readOnly = true)
	public List<MemoSummary> listMemos(UUID ownerId) {
		return listMemos(ownerId, null);
	}

	@Transactional(readOnly = true)
	public List<MemoSummary> listMemos(UUID ownerId, String folder) {
		List<VoiceMemo> memos;
		if (folder != null && !folder.isBlank()) {
			memos = voiceMemoRepository.findByOwnerIdAndFolderOrderByCreatedAtDesc(ownerId, folder);
		} else {
			memos = voiceMemoRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
		}
		return memos.stream()
			.map(this::toSummary)
			.toList();
	}

	@Transactional(readOnly = true)
	public MemoDetail getMemo(UUID ownerId, UUID memoId) {
		VoiceMemo memo = findOwnedMemo(ownerId, memoId);
		AudioAsset audio = findAudio(memoId);
		return MemoDetail.from(memo, audio);
	}

	@Transactional(readOnly = true)
	public AudioDownload getAudio(UUID ownerId, UUID memoId) {
		findOwnedMemo(ownerId, memoId);
		AudioAsset audio = findAudio(memoId);
		StoragePort.StoredResource stored = storagePort.get(audio.getStorageKey());
		return new AudioDownload(stored.resource(), audio.getContentType(), stored.contentLength());
	}

	@Transactional
	public MemoTitleUpdateResult updateTitle(UUID ownerId, UUID memoId, UpdateMemoTitleCommand command) {
		String title = command.title();
		if (title == null || title.trim().isEmpty()) {
			throw new InvalidMemoTitleException("Title cannot be blank.");
		}
		title = title.trim();
		if (title.length() > 200) {
			throw new InvalidMemoTitleException("Title cannot exceed 200 characters.");
		}

		VoiceMemo memo = findOwnedMemo(ownerId, memoId);
		memo.setTitle(title);
		voiceMemoRepository.save(memo);

		return new MemoTitleUpdateResult(memo.getId() != null ? memo.getId().toString() : null, memo.getTitle());
	}

	@Transactional
	public MemoMetadataUpdateResult updateMetadata(UUID ownerId, UUID memoId, UpdateMemoMetadataCommand command) {
		if (command.hasFolder()) {
			String folder = command.folder();
			if (folder != null && !folder.trim().isEmpty()) {
				folder = folder.trim();
				if (folder.length() > 80) {
					throw new InvalidMemoFolderException("Folder name cannot exceed 80 characters.");
				}
			}
		}

		if (command.hasTitle() && command.title() != null) {
			String title = command.title();
			if (title.trim().isEmpty()) {
				throw new InvalidMemoTitleException("Title cannot be blank.");
			}
			title = title.trim();
			if (title.length() > 200) {
				throw new InvalidMemoTitleException("Title cannot exceed 200 characters.");
			}
		}

		VoiceMemo memo = findOwnedMemo(ownerId, memoId);

		if (command.hasTitle() && command.title() != null) {
			memo.setTitle(command.title().trim());
		}

		if (command.hasFolder()) {
			String folder = command.folder();
			if (folder == null || folder.trim().isEmpty()) {
				memo.setFolder(null);
			} else {
				memo.setFolder(folder.trim());
			}
		}

		voiceMemoRepository.save(memo);

		return new MemoMetadataUpdateResult(
			memo.getId() != null ? memo.getId().toString() : null,
			memo.getTitle(),
			memo.getFolder()
		);
	}

	private MemoSummary toSummary(VoiceMemo memo) {
		AudioAsset audio = findAudio(memo.getId());
		return MemoSummary.from(memo, audio);
	}

	private VoiceMemo findOwnedMemo(UUID ownerId, UUID memoId) {
		return voiceMemoRepository.findByIdAndOwnerId(memoId, ownerId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));
	}

	private AudioAsset findAudio(UUID memoId) {
		return audioAssetRepository.findByMemoId(memoId)
			.orElseThrow(() -> new MemoNotFoundException(memoId));
	}

	public record MemoSummary(
		String id,
		String title,
		String folder,
		String recordingStatus,
		String transcriptionStatus,
		String createdAt,
		Integer durationSeconds,
		long audioSizeBytes
	) {
		static MemoSummary from(VoiceMemo memo, AudioAsset audio) {
			return new MemoSummary(
				memo.getId().toString(),
				memo.getTitle(),
				memo.getFolder(),
				memo.getRecordingStatus().name(),
				memo.getTranscriptionStatus().name(),
				memo.getCreatedAt() != null ? memo.getCreatedAt().toString() : null,
				audio.getDurationSeconds(),
				audio.getSizeBytes()
			);
		}
	}

	public record MemoDetail(
		String id,
		String title,
		String folder,
		String recordingStatus,
		String transcriptionStatus,
		String createdAt,
		String updatedAt,
		AudioMetadata audio
	) {
		static MemoDetail from(VoiceMemo memo, AudioAsset audio) {
			return new MemoDetail(
				memo.getId().toString(),
				memo.getTitle(),
				memo.getFolder(),
				memo.getRecordingStatus().name(),
				memo.getTranscriptionStatus().name(),
				memo.getCreatedAt() != null ? memo.getCreatedAt().toString() : null,
				memo.getUpdatedAt() != null ? memo.getUpdatedAt().toString() : null,
				AudioMetadata.from(audio)
			);
		}
	}

	public record AudioMetadata(
		String contentType,
		long sizeBytes,
		String originalFilename,
		Integer durationSeconds
	) {
		static AudioMetadata from(AudioAsset audio) {
			return new AudioMetadata(
				audio.getContentType(),
				audio.getSizeBytes(),
				audio.getOriginalFilename(),
				audio.getDurationSeconds()
			);
		}
	}

	public record AudioDownload(
		org.springframework.core.io.Resource resource,
		String contentType,
		long contentLength
	) {
	}

	public record UpdateMemoTitleCommand(String title) {}

	public record MemoTitleUpdateResult(String id, String title) {}

	public static class UpdateMemoMetadataCommand {
		private String title;
		private String folder;
		private boolean hasTitle = false;
		private boolean hasFolder = false;

		public UpdateMemoMetadataCommand() {}

		public UpdateMemoMetadataCommand(String title, String folder) {
			this.title = title;
			this.folder = folder;
			this.hasTitle = true;
			this.hasFolder = true;
		}

		public String title() { return title; }
		public String folder() { return folder; }
		public void setTitle(String title) { this.title = title; this.hasTitle = true; }
		public void setFolder(String folder) { this.folder = folder; this.hasFolder = true; }
		public boolean hasTitle() { return hasTitle; }
		public boolean hasFolder() { return hasFolder; }
	}

	public record MemoMetadataUpdateResult(String id, String title, String folder) {}
}
