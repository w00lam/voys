package com.voys.transcription.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptRepository extends JpaRepository<Transcript, UUID> {

	Optional<Transcript> findByMemoId(UUID memoId);
}
