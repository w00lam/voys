package com.voys.transcription.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptSegmentRepository extends JpaRepository<TranscriptSegment, UUID> {

	void deleteByTranscript(Transcript transcript);

	List<TranscriptSegment> findByTranscriptOrderByPositionAsc(Transcript transcript);
}
