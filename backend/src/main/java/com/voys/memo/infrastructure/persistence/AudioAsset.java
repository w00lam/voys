package com.voys.memo.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audio_assets")
public class AudioAsset {

	@Id
	@GeneratedValue
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "memo_id", nullable = false, unique = true)
	private VoiceMemo memo;

	@Column(nullable = false, length = 500)
	private String storageKey;

	@Column(nullable = false, length = 120)
	private String contentType;

	@Column(nullable = false)
	private long sizeBytes;

	@Column(length = 255)
	private String originalFilename;

	private Integer durationSeconds;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected AudioAsset() {
	}

	private AudioAsset(
		VoiceMemo memo,
		String storageKey,
		String contentType,
		long sizeBytes,
		String originalFilename,
		Integer durationSeconds
	) {
		this.memo = memo;
		this.storageKey = storageKey;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.originalFilename = originalFilename;
		this.durationSeconds = durationSeconds;
	}

	public static AudioAsset create(
		VoiceMemo memo,
		String storageKey,
		String contentType,
		long sizeBytes,
		String originalFilename,
		Integer durationSeconds
	) {
		return new AudioAsset(memo, storageKey, contentType, sizeBytes, originalFilename, durationSeconds);
	}

	@PrePersist
	void prePersist() {
		createdAt = Instant.now();
	}

	public String getStorageKey() {
		return storageKey;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public Integer getDurationSeconds() {
		return durationSeconds;
	}
}
