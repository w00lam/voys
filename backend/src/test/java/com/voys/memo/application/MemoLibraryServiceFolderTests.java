package com.voys.memo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.memo.domain.InvalidMemoFolderException;
import com.voys.memo.domain.MemoNotFoundException;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;

class MemoLibraryServiceFolderTests {

	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID MEMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final VoiceMemoRepository voiceMemoRepository = mock(VoiceMemoRepository.class);
	private final AudioAssetRepository audioAssetRepository = mock(AudioAssetRepository.class);
	private final StoragePort storagePort = mock(StoragePort.class);
	private final MemoLibraryService service = new MemoLibraryService(
		voiceMemoRepository,
		audioAssetRepository,
		storagePort
	);

	@Test
	void updateFolderTrimsAndPersistsOwnedMemoFolder() {
		VoiceMemo memo = memo("Lecture about product strategy");
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));

		MemoLibraryService.MemoMetadataUpdateResult result = service.updateMetadata(
			OWNER_ID,
			MEMO_ID,
			new MemoLibraryService.UpdateMemoMetadataCommand(null, "  Work  ")
		);

		assertThat(result.folder()).isEqualTo("Work");
		assertThat(memo.getFolder()).isEqualTo("Work");
		assertThat(memo.getTitle()).isEqualTo("Lecture about product strategy");
	}

	@Test
	void updateFolderClearsBlankFolder() {
		VoiceMemo memo = memo("Lecture about product strategy");
		memo.setFolder("Work");
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));

		MemoLibraryService.MemoMetadataUpdateResult result = service.updateMetadata(
			OWNER_ID,
			MEMO_ID,
			new MemoLibraryService.UpdateMemoMetadataCommand(null, "   ")
		);

		assertThat(result.folder()).isNull();
		assertThat(memo.getFolder()).isNull();
	}

	@Test
	void updateFolderRejectsOverlongFolderBeforeRepositoryLookup() {
		String overlongFolder = "a".repeat(81);

		assertThatThrownBy(() -> service.updateMetadata(
			OWNER_ID,
			MEMO_ID,
			new MemoLibraryService.UpdateMemoMetadataCommand(null, overlongFolder)
		))
			.isInstanceOf(InvalidMemoFolderException.class)
			.hasMessageContaining("Folder");

		verifyNoInteractions(voiceMemoRepository, audioAssetRepository, storagePort);
	}

	@Test
	void updateFolderUsesOwnershipScopedLookup() {
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateMetadata(
			OWNER_ID,
			MEMO_ID,
			new MemoLibraryService.UpdateMemoMetadataCommand(null, "Work")
		))
			.isInstanceOf(MemoNotFoundException.class);
	}

	@Test
	void listMemosCanFilterByFolderAndReturnsFolderMetadata() {
		VoiceMemo memo = memo("Lecture about product strategy");
		memo.setFolder("Work");
		when(voiceMemoRepository.findByOwnerIdAndFolderOrderByCreatedAtDesc(OWNER_ID, "Work"))
			.thenReturn(List.of(memo));
		when(audioAssetRepository.findByMemoId(memo.getId())).thenReturn(Optional.of(audio(memo)));

		List<MemoLibraryService.MemoSummary> results = service.listMemos(OWNER_ID, "Work");

		assertThat(results).hasSize(1);
		assertThat(results.getFirst().folder()).isEqualTo("Work");
	}

	private VoiceMemo memo(String title) {
		UserAccount owner = UserAccount.create("user@example.com", "Voys User", "hash");
		VoiceMemo memo = VoiceMemo.createUploaded(owner, title);
		ReflectionTestUtils.setField(memo, "id", MEMO_ID);
		return memo;
	}

	private AudioAsset audio(VoiceMemo memo) {
		return AudioAsset.create(memo, "memos/audio.webm", "audio/webm", 1024, "audio.webm", 120);
	}
}
