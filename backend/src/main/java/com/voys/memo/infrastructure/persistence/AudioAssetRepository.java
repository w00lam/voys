package com.voys.memo.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioAssetRepository extends JpaRepository<AudioAsset, UUID> {
}
