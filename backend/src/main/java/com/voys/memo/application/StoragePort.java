package com.voys.memo.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.springframework.core.io.Resource;

public interface StoragePort {

	StoredObject store(StoreObjectRequest request);

	StoredResource get(String storageKey);

	record StoreObjectRequest(
		String folder,
		String extension,
		InputStream inputStream
	) {
	}

	record StoredObject(
		String storageKey
	) {
	}

	record StoredResource(
		Resource resource,
		long contentLength,
		Path localPath
	) {
	}

	class StorageException extends RuntimeException {

		public StorageException(String message, IOException cause) {
			super(message, cause);
		}
	}
}
