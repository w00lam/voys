package com.voys.notes.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "generated_notes")
public class GeneratedNote {

	@Id
	@Column(name = "memo_id")
	private UUID memoId;

	@Column(nullable = false)
	private String status;

	@Column(columnDefinition = "text")
	private String summary;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "generated_note_key_points", joinColumns = @JoinColumn(name = "memo_id"))
	@Column(name = "key_point", columnDefinition = "text")
	private List<String> keyPoints;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "generated_note_action_items", joinColumns = @JoinColumn(name = "memo_id"))
	@Column(name = "action_item", columnDefinition = "text")
	private List<String> actionItems;

	@Column(name = "failure_reason", columnDefinition = "text")
	private String failureReason;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected GeneratedNote() {
	}

	public GeneratedNote(UUID memoId, String status, String summary, List<String> keyPoints, List<String> actionItems, String failureReason, Instant updatedAt) {
		this.memoId = memoId;
		this.status = status;
		this.summary = summary;
		this.keyPoints = keyPoints;
		this.actionItems = actionItems;
		this.failureReason = failureReason;
		this.updatedAt = updatedAt;
	}

	public UUID getMemoId() {
		return memoId;
	}

	public String getStatus() {
		return status;
	}

	public String getSummary() {
		return summary;
	}

	public List<String> getKeyPoints() {
		return keyPoints;
	}

	public List<String> getActionItems() {
		return actionItems;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
