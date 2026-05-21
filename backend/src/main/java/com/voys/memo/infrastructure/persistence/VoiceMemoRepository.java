package com.voys.memo.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceMemoRepository extends JpaRepository<VoiceMemo, UUID> {
}
