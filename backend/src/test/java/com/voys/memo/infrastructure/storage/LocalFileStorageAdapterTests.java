package com.voys.memo.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.voys.memo.application.StoragePort.StoreObjectRequest;

class LocalFileStorageAdapterTests {

	@TempDir
	Path storageRoot;

	@Test
	void storeWritesFileUnderConfiguredRoot() throws Exception {
		LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(storageRoot);

		var stored = adapter.store(new StoreObjectRequest(
			"memos/test",
			"webm",
			new ByteArrayInputStream("audio".getBytes())
		));

		Path storedPath = storageRoot.resolve(stored.storageKey()).normalize();
		assertThat(storedPath).startsWith(storageRoot);
		assertThat(Files.readString(storedPath)).isEqualTo("audio");
		assertThat(stored.storageKey()).startsWith("memos/test/");
		assertThat(stored.storageKey()).endsWith(".webm");
	}

	@Test
	void getReadsStoredFile() throws Exception {
		LocalFileStorageAdapter adapter = new LocalFileStorageAdapter(storageRoot);
		var stored = adapter.store(new StoreObjectRequest(
			"memos/test",
			"webm",
			new ByteArrayInputStream("audio".getBytes())
		));

		var resource = adapter.get(stored.storageKey());

		assertThat(resource.contentLength()).isEqualTo(5);
		assertThat(resource.localPath()).startsWith(storageRoot);
		assertThat(resource.resource().getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
			.isEqualTo("audio");
	}
}
