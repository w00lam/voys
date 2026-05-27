package com.voys.memo.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.memo.domain.RecordingStatus;
import com.voys.memo.domain.TranscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "voice_memos")
public class VoiceMemo {

	@Id
	@GeneratedValue
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private UserAccount owner;

	@Column(nullable = false, length = 200)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private RecordingStatus recordingStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private TranscriptionStatus transcriptionStatus;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@Column(name = "failure_reason_code", length = 50)
	private String failureReasonCode;

	@Column(name = "failure_reason_message", length = 255)
	private String failureReasonMessage;

	@Column(name = "failure_reason_retryable")
	private Boolean failureReasonRetryable;

	@Column(name = "suggested_title", length = 200)
	private String suggestedTitle;

	@Column(name = "folder", length = 80)
	private String folder;

	protected VoiceMemo() {
	}

	private VoiceMemo(UserAccount owner, String title) {
		this.owner = owner;
		this.title = title;
		this.recordingStatus = RecordingStatus.UPLOADED;
		this.transcriptionStatus = TranscriptionStatus.PENDING;
	}

	public static VoiceMemo createUploaded(UserAccount owner, String title) {
		return new VoiceMemo(owner, title);
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

	public UUID getId() {
		return id;
	}

	public UserAccount getOwner() {
		return owner;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public RecordingStatus getRecordingStatus() {
		return recordingStatus;
	}

	public TranscriptionStatus getTranscriptionStatus() {
		return transcriptionStatus;
	}

	public void markTranscriptionProcessing() {
		transcriptionStatus = TranscriptionStatus.PROCESSING;
		failureReasonCode = null;
		failureReasonMessage = null;
		failureReasonRetryable = null;
	}

	public void markTranscriptionCompleted() {
		transcriptionStatus = TranscriptionStatus.COMPLETED;
	}

	public void markTranscriptionFailed() {
		transcriptionStatus = TranscriptionStatus.FAILED;
	}

	public void markTranscriptionFailed(String code, String message, boolean retryable) {
		transcriptionStatus = TranscriptionStatus.FAILED;
		this.failureReasonCode = code;
		this.failureReasonMessage = message;
		this.failureReasonRetryable = retryable;
	}

	public String getFailureReasonCode() {
		return failureReasonCode;
	}

	public String getFailureReasonMessage() {
		return failureReasonMessage;
	}

	public Boolean getFailureReasonRetryable() {
		return failureReasonRetryable;
	}

	public String getSuggestedTitle() {
		return suggestedTitle;
	}

	public void setSuggestedTitle(String suggestedTitle) {
		this.suggestedTitle = suggestedTitle;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
