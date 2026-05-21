package com.voys.memo.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.voys.memo.application.StoragePort;

@Component
public class LocalFileStorageAdapter implements StoragePort {

	private final Path root;

	public LocalFileStorageAdapter(@Value("${voys.storage.root}") Path root) {
		this.root = root.toAbsolutePath().normalize();
	}

	@Override
	public StoredObject store(StoreObjectRequest request) {
		String extension = sanitizeExtension(request.extension());
		String storageKey = request.folder() + "/" + UUID.randomUUID() + extension;
		Path target = root.resolve(storageKey).normalize();

		if (!target.startsWith(root)) {
			throw new IllegalArgumentException("Storage key escapes the configured root.");
		}

		try {
			Files.createDirectories(target.getParent());
			Files.copy(request.inputStream(), target);
			return new StoredObject(storageKey);
		} catch (IOException exception) {
			throw new StorageException("Failed to store uploaded recording.", exception);
		}
	}

	@Override
	public StoredResource get(String storageKey) {
		Path target = resolveStorageKey(storageKey);
		try {
			if (!Files.isRegularFile(target)) {
				throw new IllegalArgumentException("Stored object was not found.");
			}

			return new StoredResource(new FileSystemResource(target), Files.size(target));
		} catch (IOException exception) {
			throw new StorageException("Failed to read stored recording.", exception);
		}
	}

	private Path resolveStorageKey(String storageKey) {
		Path target = root.resolve(storageKey).normalize();
		if (!target.startsWith(root)) {
			throw new IllegalArgumentException("Storage key escapes the configured root.");
		}

		return target;
	}

	private static String sanitizeExtension(String extension) {
		if (extension == null || extension.isBlank()) {
			return "";
		}

		String normalized = extension.startsWith(".") ? extension : "." + extension;
		if (!normalized.matches("\\.[a-zA-Z0-9]{1,12}")) {
			return "";
		}

		return normalized.toLowerCase();
	}
}
