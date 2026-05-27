package com.voys.notes.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.voys.identity.application.UserPrincipal;
import com.voys.notes.application.GeneratedNoteService;

class GeneratedNoteControllerTests {

	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID MEMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final GeneratedNoteService generatedNoteService = mock(GeneratedNoteService.class);
	private final GeneratedNoteController controller = new GeneratedNoteController(generatedNoteService);

	@Test
	void generateNoteUsesAuthenticatedOwnerAndMemoId() {
		UserPrincipal principal = new UserPrincipal(OWNER_ID, "user@example.com", "Voys User", "hash");
		GeneratedNoteService.GeneratedNoteResponse expected = response();
		when(generatedNoteService.generate(OWNER_ID, MEMO_ID)).thenReturn(expected);

		assertThat(controller.generate(principal, MEMO_ID)).isEqualTo(expected);

		verify(generatedNoteService).generate(OWNER_ID, MEMO_ID);
	}

	@Test
	void readNoteUsesAuthenticatedOwnerAndMemoId() {
		UserPrincipal principal = new UserPrincipal(OWNER_ID, "user@example.com", "Voys User", "hash");
		GeneratedNoteService.GeneratedNoteResponse expected = response();
		when(generatedNoteService.getGeneratedNote(OWNER_ID, MEMO_ID)).thenReturn(expected);

		assertThat(controller.getGeneratedNote(principal, MEMO_ID)).isEqualTo(expected);

		verify(generatedNoteService).getGeneratedNote(OWNER_ID, MEMO_ID);
	}

	@Test
	void updateNoteUsesAuthenticatedOwnerAndRequestBody() {
		UserPrincipal principal = new UserPrincipal(OWNER_ID, "user@example.com", "Voys User", "hash");
		GeneratedNoteService.UpdateGeneratedNoteCommand request =
			new GeneratedNoteService.UpdateGeneratedNoteCommand("Edited", List.of("Point"), List.of("Action"));
		GeneratedNoteService.GeneratedNoteResponse expected = response();
		when(generatedNoteService.updateGeneratedNote(OWNER_ID, MEMO_ID, request)).thenReturn(expected);

		assertThat(controller.updateGeneratedNote(principal, MEMO_ID, request)).isEqualTo(expected);

		verify(generatedNoteService).updateGeneratedNote(OWNER_ID, MEMO_ID, request);
	}

	@Test
	void exportGeneratedNoteReturnsPlainTextResponse() {
		UserPrincipal principal = new UserPrincipal(OWNER_ID, "user@example.com", "Voys User", "hash");
		when(generatedNoteService.exportGeneratedNote(OWNER_ID, MEMO_ID)).thenReturn("Summary\nEdited");

		ResponseEntity<String> response = controller.exportGeneratedNote(principal, MEMO_ID);

		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
		assertThat(response.getBody()).isEqualTo("Summary\nEdited");
		verify(generatedNoteService).exportGeneratedNote(OWNER_ID, MEMO_ID);
	}

	@Test
	void exportTranscriptReturnsPlainTextResponse() {
		UserPrincipal principal = new UserPrincipal(OWNER_ID, "user@example.com", "Voys User", "hash");
		when(generatedNoteService.exportTranscript(OWNER_ID, MEMO_ID)).thenReturn("Transcript text");

		ResponseEntity<String> response = controller.exportTranscript(principal, MEMO_ID);

		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
		assertThat(response.getBody()).isEqualTo("Transcript text");
		verify(generatedNoteService).exportTranscript(OWNER_ID, MEMO_ID);
	}

	private GeneratedNoteService.GeneratedNoteResponse response() {
		return new GeneratedNoteService.GeneratedNoteResponse(
			MEMO_ID.toString(),
			"GENERATED",
			"The team reviewed launch strategy.",
			List.of("Launch risks"),
			List.of("Follow up on owners"),
			null,
			"2026-05-27T15:30:00Z"
		);
	}
}
