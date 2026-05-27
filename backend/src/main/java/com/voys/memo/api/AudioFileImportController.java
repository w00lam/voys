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
import com.voys.memo.application.AudioFileImportService;
import com.voys.memo.domain.InvalidRecordingException;

@RestController
public class AudioFileImportController {

	private final AudioFileImportService audioFileImportService;

	public AudioFileImportController(AudioFileImportService audioFileImportService) {
		this.audioFileImportService = audioFileImportService;
	}

	@PostMapping("/api/memos/audio-files")
	@ResponseStatus(HttpStatus.CREATED)
	public AudioFileImportService.ImportedAudioResult importAudio(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestParam("audio") MultipartFile audio,
		@RequestParam(value = "durationSeconds", required = false) Integer durationSeconds
	) {
		try {
			return audioFileImportService.importAudio(new AudioFileImportService.ImportAudioCommand(
				principal.id(),
				audio.getOriginalFilename(),
				audio.getContentType(),
				audio.getSize(),
				durationSeconds,
				audio.getInputStream()
			));
		} catch (IOException e) {
			throw new InvalidRecordingException("Imported file could not be read.");
		}
	}
}
