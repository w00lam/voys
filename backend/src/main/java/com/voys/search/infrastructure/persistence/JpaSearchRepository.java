package com.voys.search.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.voys.search.application.SearchRepository;
import com.voys.search.application.SearchResult;

@Repository
public class JpaSearchRepository implements SearchRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@Override
	public List<SearchResult> search(UUID ownerId, String query, int limit) {
		String sql = """
			SELECT 
				m.id AS memoId, 
				m.title AS title, 
				'TITLE' AS matchType, 
				m.title AS snippet, 
				m.transcription_status AS transcriptionStatus
			FROM voice_memos m
			WHERE m.owner_id = :ownerId 
			  AND LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))

			UNION ALL

			SELECT 
				m.id AS memoId, 
				m.title AS title, 
				'TRANSCRIPT' AS matchType, 
				t.text AS snippet, 
				m.transcription_status AS transcriptionStatus
			FROM voice_memos m
			JOIN transcripts t ON m.id = t.memo_id
			WHERE m.owner_id = :ownerId 
			  AND m.transcription_status = 'COMPLETED'
			  AND LOWER(t.text) LIKE LOWER(CONCAT('%', :query, '%'))
			""";

		List<Object[]> rows = entityManager.createNativeQuery(sql)
			.setParameter("ownerId", ownerId)
			.setParameter("query", query)
			.setMaxResults(limit)
			.getResultList();

		return rows.stream().map(row -> new SearchResult(
			row[0].toString(),
			(String) row[1],
			(String) row[2],
			(String) row[3],
			(String) row[4]
		)).toList();
	}
}
