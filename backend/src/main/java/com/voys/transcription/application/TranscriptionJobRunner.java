package com.voys.transcription.application;

public interface TranscriptionJobRunner {
	void submit(Runnable job);
}
