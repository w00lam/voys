package com.voys.notes.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voys.notes.domain.GeneratedNote;

@Repository
public interface GeneratedNoteRepository extends JpaRepository<GeneratedNote, UUID> {
}
