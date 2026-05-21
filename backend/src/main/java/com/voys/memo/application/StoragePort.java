package com.voys.memo.application;

import java.io.IOException;
import java.io.InputStream;

public interface StoragePort {

	StoredObject store(StoreObjectRequest request);

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

	class StorageException extends RuntimeException {

		public StorageException(String message, IOException cause) {
			super(message, cause);
		}
	}
}
