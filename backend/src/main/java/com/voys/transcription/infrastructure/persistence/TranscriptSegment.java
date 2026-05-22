package com.voys.transcription.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "transcript_segments",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_transcript_segments_transcript_position",
		columnNames = {"transcript_id", "position"}
	)
)
public class TranscriptSegment {

	@Id
	@GeneratedValue
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "transcript_id", nullable = false)
	private Transcript transcript;

	@Column(name = "position", nullable = false)
	private int position;

	@Column(name = "start_seconds", nullable = false)
	private double startSeconds;

	@Column(name = "end_seconds", nullable = false)
	private double endSeconds;

	@Lob
	@Column(nullable = false)
	private String text;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected TranscriptSegment() {
	}

	private TranscriptSegment(Transcript transcript, int position, double startSeconds, double endSeconds, String text) {
		this.transcript = transcript;
		this.position = position;
		this.startSeconds = startSeconds;
		this.endSeconds = endSeconds;
		this.text = text;
	}

	public static TranscriptSegment create(Transcript transcript, int position, double startSeconds, double endSeconds, String text) {
		return new TranscriptSegment(transcript, position, startSeconds, endSeconds, text);
	}

	@PrePersist
	void prePersist() {
		createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public Transcript getTranscript() {
		return transcript;
	}

	public int getPosition() {
		return position;
	}

	public double getStartSeconds() {
		return startSeconds;
	}

	public double getEndSeconds() {
		return endSeconds;
	}

	public String getText() {
		return text;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
