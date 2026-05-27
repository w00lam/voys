package com.voys.memo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.memo.domain.InvalidMemoTitleException;
import com.voys.memo.domain.MemoNotFoundException;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;

class MemoLibraryServiceTitleUpdateTests {

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
	void updateTitleTrimsAndPersistsOwnedMemoTitle() {
		UserAccount owner = UserAccount.create("user@example.com", "Voys User", "hash");
		VoiceMemo memo = VoiceMemo.createUploaded(owner, "Recording 2026-05-27 12:00");
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.of(memo));

		MemoLibraryService.MemoTitleUpdateResult result = service.updateTitle(
			OWNER_ID,
			MEMO_ID,
			new MemoLibraryService.UpdateMemoTitleCommand("  Product strategy sync  ")
		);

		assertThat(result.title()).isEqualTo("Product strategy sync");
		assertThat(memo.getTitle()).isEqualTo("Product strategy sync");
	}

	@Test
	void updateTitleRejectsBlankTitlesBeforeRepositoryLookup() {
		assertThatThrownBy(() -> service.updateTitle(
			OWNER_ID,
			MEMO_ID,
			new MemoLibraryService.UpdateMemoTitleCommand("   ")
		))
			.isInstanceOf(InvalidMemoTitleException.class)
			.hasMessageContaining("Title");

		verifyNoInteractions(voiceMemoRepository, audioAssetRepository, storagePort);
	}

	@Test
	void updateTitleUsesOwnershipScopedLookup() {
		when(voiceMemoRepository.findByIdAndOwnerId(MEMO_ID, OWNER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateTitle(
			OWNER_ID,
			MEMO_ID,
			new MemoLibraryService.UpdateMemoTitleCommand("Renamed memo")
		))
			.isInstanceOf(MemoNotFoundException.class);
	}
}
