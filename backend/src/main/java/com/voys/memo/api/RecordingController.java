package com.voys.memo.api;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.voys.identity.application.UserPrincipal;
import com.voys.memo.application.RecordingUploadService;
import com.voys.memo.application.RecordingUploadService.RecordingUploadCommand;
import com.voys.memo.application.RecordingUploadService.RecordingUploadResult;
import com.voys.memo.domain.InvalidRecordingException;

@RestController
public class RecordingController {

	private final RecordingUploadService recordingUploadService;

	public RecordingController(RecordingUploadService recordingUploadService) {
		this.recordingUploadService = recordingUploadService;
	}

	@PostMapping("/api/memos/recordings")
	@ResponseStatus(HttpStatus.CREATED)
	public RecordingUploadResult uploadRecording(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestParam("audio") MultipartFile audio,
		@RequestParam(value = "durationSeconds", required = false) Integer durationSeconds
	) {
		try {
			return recordingUploadService.upload(new RecordingUploadCommand(
				principal.id(),
				audio.getOriginalFilename(),
				audio.getContentType(),
				audio.getSize(),
				durationSeconds,
				audio.getInputStream()
			));
		} catch (IOException exception) {
			throw new InvalidRecordingException("Recording file could not be read.");
		}
	}
}
