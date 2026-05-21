package com.voys.transcription.infrastructure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.voys.transcription.application.TranscriptionJobRunner;

import jakarta.annotation.PreDestroy;

@Component
public class ExecutorTranscriptionJobRunner implements TranscriptionJobRunner {

	private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "voys-transcription-worker");
		thread.setDaemon(true);
		return thread;
	});

	@Override
	public void submit(Runnable job) {
		executorService.submit(job);
	}

	@PreDestroy
	public void shutdown() {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}
