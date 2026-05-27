package com.voys.notes.api;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import com.voys.identity.application.UserPrincipal;
import com.voys.notes.application.GeneratedNoteService;
import com.voys.notes.application.GeneratedNoteService.GeneratedNoteResponse;
import com.voys.notes.application.GeneratedNoteService.UpdateGeneratedNoteCommand;

@RestController
public class GeneratedNoteController {

	private final GeneratedNoteService generatedNoteService;

	public GeneratedNoteController(GeneratedNoteService generatedNoteService) {
		this.generatedNoteService = generatedNoteService;
	}

	@PostMapping("/api/memos/{memoId}/generated-note")
	public GeneratedNoteResponse generate(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		return generatedNoteService.generate(principal.id(), memoId);
	}

	@GetMapping("/api/memos/{memoId}/generated-note")
	public GeneratedNoteResponse getGeneratedNote(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		return generatedNoteService.getGeneratedNote(principal.id(), memoId);
	}

	@PatchMapping("/api/memos/{memoId}/generated-note")
	public GeneratedNoteResponse updateGeneratedNote(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId,
		@RequestBody UpdateGeneratedNoteCommand command
	) {
		return generatedNoteService.updateGeneratedNote(principal.id(), memoId, command);
	}

	@GetMapping("/api/memos/{memoId}/generated-note/export")
	public ResponseEntity<String> exportGeneratedNote(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		String text = generatedNoteService.exportGeneratedNote(principal.id(), memoId);
		return ResponseEntity.ok()
			.contentType(MediaType.TEXT_PLAIN)
			.body(text);
	}

	@GetMapping("/api/memos/{memoId}/transcript/export")
	public ResponseEntity<String> exportTranscript(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		String text = generatedNoteService.exportTranscript(principal.id(), memoId);
		return ResponseEntity.ok()
			.contentType(MediaType.TEXT_PLAIN)
			.body(text);
	}
}
