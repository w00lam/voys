package com.voys.transcription.api;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voys.identity.application.UserPrincipal;
import com.voys.transcription.application.TranscriptionWorkflowService;
import com.voys.transcription.application.TranscriptionWorkflowService.TranscriptionResponse;

@RestController
public class TranscriptionController {

	private final TranscriptionWorkflowService transcriptionWorkflowService;

	public TranscriptionController(TranscriptionWorkflowService transcriptionWorkflowService) {
		this.transcriptionWorkflowService = transcriptionWorkflowService;
	}

	@PostMapping("/api/memos/{memoId}/transcription")
	public TranscriptionResponse startTranscription(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		return transcriptionWorkflowService.startTranscription(principal.id(), memoId);
	}

	@GetMapping("/api/memos/{memoId}/transcript")
	public TranscriptionResponse getTranscript(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		return transcriptionWorkflowService.getTranscript(principal.id(), memoId);
	}
}
