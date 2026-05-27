package com.voys.notes.api;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voys.identity.application.UserPrincipal;
import com.voys.notes.application.GeneratedNoteService;
import com.voys.notes.application.GeneratedNoteService.GeneratedNoteResponse;

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
}
