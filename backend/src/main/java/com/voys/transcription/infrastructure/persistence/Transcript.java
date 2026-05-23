package com.voys.transcription.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import com.voys.memo.infrastructure.persistence.VoiceMemo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "transcripts")
public class Transcript {

	@Id
	@GeneratedValue
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "memo_id", nullable = false, unique = true)
	private VoiceMemo memo;

	@Column(nullable = false, columnDefinition = "text")
	private String text;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected Transcript() {
	}

	private Transcript(VoiceMemo memo, String text) {
		this.memo = memo;
		this.text = text;
	}

	public static Transcript create(VoiceMemo memo, String text) {
		return new Transcript(memo, text);
	}

	public void replaceText(String text) {
		this.text = text;
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public String getText() {
		return text;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
