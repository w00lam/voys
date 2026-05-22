package com.voys.memo.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.voys.identity.infrastructure.persistence.UserAccountRepository;
import com.voys.memo.domain.InvalidRecordingException;
import com.voys.memo.infrastructure.persistence.AudioAssetRepository;
import com.voys.memo.infrastructure.persistence.VoiceMemoRepository;

class RecordingUploadServiceTests {

	private static final long MAX_UPLOAD_BYTES = 524_288_000L;
	private static final int MAX_DURATION_SECONDS = 7_200;

	private final UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
	private final VoiceMemoRepository voiceMemoRepository = mock(VoiceMemoRepository.class);
	private final AudioAssetRepository audioAssetRepository = mock(AudioAssetRepository.class);
	private final StoragePort storagePort = mock(StoragePort.class);
	private final TemporaryTitleGenerator titleGenerator = mock(TemporaryTitleGenerator.class);

	private final RecordingUploadService service = new RecordingUploadService(
		userAccountRepository,
		voiceMemoRepository,
		audioAssetRepository,
		storagePort,
		titleGenerator,
		MAX_UPLOAD_BYTES
	);

	@Test
	void uploadRejectsRecordingsLongerThanTwoHoursBeforeStorage() {
		var command = commandWithDuration(MAX_DURATION_SECONDS + 1);

		assertThatThrownBy(() -> service.upload(command))
			.isInstanceOf(InvalidRecordingException.class)
			.hasMessageContaining("2 hours");

		verifyNoInteractions(userAccountRepository, voiceMemoRepository, audioAssetRepository, storagePort);
	}

	@Test
	void uploadRejectsNonPositiveDurationsBeforeStorage() {
		var command = commandWithDuration(0);

		assertThatThrownBy(() -> service.upload(command))
			.isInstanceOf(InvalidRecordingException.class)
			.hasMessageContaining("duration");

		verifyNoInteractions(userAccountRepository, voiceMemoRepository, audioAssetRepository, storagePort);
	}

	private RecordingUploadService.RecordingUploadCommand commandWithDuration(Integer durationSeconds) {
		return new RecordingUploadService.RecordingUploadCommand(
			UUID.fromString("11111111-1111-1111-1111-111111111111"),
			"recording.webm",
			"audio/webm;codecs=opus",
			1024,
			durationSeconds,
			new ByteArrayInputStream("audio".getBytes())
		);
	}
}
