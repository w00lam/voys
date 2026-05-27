package com.voys.memo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.identity.infrastructure.persistence.UserAccountRepository;
import com.voys.memo.domain.InvalidRecordingException;
import com.voys.memo.infrastructure.persistence.AudioAsset;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;

class AudioFileImportServiceTests {

	private static final long MAX_UPLOAD_BYTES = 524_288_000L;
	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private final UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
	private final VoiceMemoRepository voiceMemoRepository = mock(VoiceMemoRepository.class);
	private final AudioAssetRepository audioAssetRepository = mock(AudioAssetRepository.class);
	private final StoragePort storagePort = mock(StoragePort.class);
	private final TemporaryTitleGenerator titleGenerator = mock(TemporaryTitleGenerator.class);

	private final AudioFileImportService service = new AudioFileImportService(
		userAccountRepository,
		voiceMemoRepository,
		audioAssetRepository,
		storagePort,
		titleGenerator,
		MAX_UPLOAD_BYTES
	);

	@Test
	void importAudioCreatesMemoWithTitleFromSanitizedFilename() {
		UserAccount owner = UserAccount.create("user@example.com", "Voys User", "hash");
		when(userAccountRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
		when(voiceMemoRepository.save(any(VoiceMemo.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(storagePort.store(any(StoragePort.StoreObjectRequest.class)))
			.thenReturn(new StoragePort.StoredObject("memos/imported-audio"));

		AudioFileImportService.ImportedAudioResult result = service.importAudio(new AudioFileImportService.ImportAudioCommand(
			OWNER_ID,
			"  Product_strategy-sync.final.mp3  ",
			"audio/mpeg",
			4096,
			3600,
			new ByteArrayInputStream("audio".getBytes())
		));

		assertThat(result.title()).isEqualTo("Product strategy sync final");

		ArgumentCaptor<AudioAsset> audioCaptor = ArgumentCaptor.forClass(AudioAsset.class);
		verify(audioAssetRepository).save(audioCaptor.capture());
		assertThat(audioCaptor.getValue().getContentType()).isEqualTo("audio/mpeg");
		assertThat(audioCaptor.getValue().getOriginalFilename()).isEqualTo("Product_strategy-sync.final.mp3");
		assertThat(audioCaptor.getValue().getDurationSeconds()).isEqualTo(3600);
	}

	@Test
	void importAudioRejectsUnsupportedContentTypesBeforeStorage() {
		AudioFileImportService.ImportAudioCommand command = new AudioFileImportService.ImportAudioCommand(
			OWNER_ID,
			"notes.txt",
			"text/plain",
			128,
			null,
			new ByteArrayInputStream("text".getBytes())
		);

		assertThatThrownBy(() -> service.importAudio(command))
			.isInstanceOf(InvalidRecordingException.class)
			.hasMessageContaining("audio");

		verifyNoInteractions(userAccountRepository, voiceMemoRepository, audioAssetRepository, storagePort);
	}

	@Test
	void importAudioFallsBackToTemporaryTitleWhenFilenameIsMissing() {
		UserAccount owner = UserAccount.create("user@example.com", "Voys User", "hash");
		when(userAccountRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
		when(titleGenerator.generate()).thenReturn("Recording 2026-05-27 12:00");
		when(voiceMemoRepository.save(any(VoiceMemo.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(storagePort.store(any(StoragePort.StoreObjectRequest.class)))
			.thenReturn(new StoragePort.StoredObject("memos/imported-audio"));

		AudioFileImportService.ImportedAudioResult result = service.importAudio(new AudioFileImportService.ImportAudioCommand(
			OWNER_ID,
			null,
			"audio/mpeg",
			4096,
			null,
			new ByteArrayInputStream("audio".getBytes())
		));

		assertThat(result.title()).isEqualTo("Recording 2026-05-27 12:00");

		ArgumentCaptor<AudioAsset> audioCaptor = ArgumentCaptor.forClass(AudioAsset.class);
		verify(audioAssetRepository).save(audioCaptor.capture());
		assertThat(audioCaptor.getValue().getOriginalFilename()).isNull();
	}
}
