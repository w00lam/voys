package com.voys.memo.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import com.voys.identity.application.UserPrincipal;
import com.voys.memo.application.AudioFileImportService;

class AudioFileImportControllerTests {

	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private final AudioFileImportService audioFileImportService = mock(AudioFileImportService.class);
	private final AudioFileImportController controller = new AudioFileImportController(audioFileImportService);

	@Test
	void importAudioUsesAuthenticatedUserAndMultipartAudio() {
		UserPrincipal principal = new UserPrincipal(OWNER_ID, "user@example.com", "Voys User", "hash");
		MockMultipartFile audio = new MockMultipartFile(
			"audio",
			"meeting.mp3",
			"audio/mpeg",
			"audio".getBytes()
		);
		AudioFileImportService.ImportedAudioResult expected = new AudioFileImportService.ImportedAudioResult(
			"33333333-3333-3333-3333-333333333333",
			"meeting",
			"UPLOADED",
			"PENDING",
			"2026-05-27T12:00:00Z"
		);
		when(audioFileImportService.importAudio(any(AudioFileImportService.ImportAudioCommand.class)))
			.thenReturn(expected);

		AudioFileImportService.ImportedAudioResult response = controller.importAudio(principal, audio, 120);

		assertThat(response).isEqualTo(expected);

		ArgumentCaptor<AudioFileImportService.ImportAudioCommand> commandCaptor =
			ArgumentCaptor.forClass(AudioFileImportService.ImportAudioCommand.class);
		verify(audioFileImportService).importAudio(commandCaptor.capture());
		assertThat(commandCaptor.getValue().ownerId()).isEqualTo(OWNER_ID);
		assertThat(commandCaptor.getValue().originalFilename()).isEqualTo("meeting.mp3");
		assertThat(commandCaptor.getValue().contentType()).isEqualTo("audio/mpeg");
		assertThat(commandCaptor.getValue().sizeBytes()).isEqualTo(5);
		assertThat(commandCaptor.getValue().durationSeconds()).isEqualTo(120);
	}
}
