package com.voys.transcription.infrastructure;

import org.springframework.stereotype.Component;

import com.voys.transcription.application.TranscriptionPort;

@Component
public class LocalWhisperAdapter implements TranscriptionPort {

	@Override
	public TranscriptionResult transcribe(TranscriptionRequest request) {
		throw new UnsupportedOperationException("Whisper CLI integration is not implemented yet");
	}
}

