package com.voys.memo.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceMemoRepository extends JpaRepository<VoiceMemo, UUID> {

	List<VoiceMemo> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	List<VoiceMemo> findByOwnerIdAndFolderOrderByCreatedAtDesc(UUID ownerId, String folder);

	Optional<VoiceMemo> findByIdAndOwnerId(UUID id, UUID ownerId);
}
