package com.voys.search.infrastructure.persistence;

import java.nio.ByteBuffer;
import java.sql.Clob;
import java.sql.SQLException;
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
				m.transcription_status AS transcriptionStatus,
				CAST(NULL AS DOUBLE PRECISION) AS segmentStartSeconds
			FROM voice_memos m
			WHERE m.owner_id = :ownerId 
			  AND LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))

			UNION ALL

			SELECT
				m.id AS memoId,
				m.title AS title,
				'TRANSCRIPT' AS matchType,
				ts.text AS snippet,
				m.transcription_status AS transcriptionStatus,
				ts.start_seconds AS segmentStartSeconds
			FROM voice_memos m
			JOIN transcripts t ON m.id = t.memo_id
			JOIN transcript_segments ts ON t.id = ts.transcript_id
			WHERE m.owner_id = :ownerId
			  AND m.transcription_status = 'COMPLETED'
			  AND LOWER(ts.text) LIKE LOWER(CONCAT('%', :query, '%'))

			UNION ALL

			SELECT
				m.id AS memoId,
				m.title AS title,
				'TRANSCRIPT' AS matchType,
				t.text AS snippet, 
				m.transcription_status AS transcriptionStatus,
				CAST(NULL AS DOUBLE PRECISION) AS segmentStartSeconds
			FROM voice_memos m
			JOIN transcripts t ON m.id = t.memo_id
			WHERE m.owner_id = :ownerId 
			  AND m.transcription_status = 'COMPLETED'
			  AND LOWER(t.text) LIKE LOWER(CONCAT('%', :query, '%'))
			  AND NOT EXISTS (
			      SELECT 1 FROM transcript_segments ts WHERE ts.transcript_id = t.id
			  )
			""";

		List<Object[]> rows = entityManager.createNativeQuery(sql)
			.setParameter("ownerId", ownerId)
			.setParameter("query", query)
			.setMaxResults(limit)
			.getResultList();

		return rows.stream().map(row -> new SearchResult(
			asUuidString(row[0]),
			asString(row[1]),
			asString(row[2]),
			asString(row[3]),
			asString(row[4]),
			asDouble(row[5])
		)).toList();
	}

	private String asUuidString(Object value) {
		if (value instanceof UUID uuid) {
			return uuid.toString();
		}
		if (value instanceof byte[] bytes) {
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			return new UUID(buffer.getLong(), buffer.getLong()).toString();
		}
		return value.toString();
	}

	private String asString(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Clob clob) {
			try {
				return clob.getSubString(1, Math.toIntExact(clob.length()));
			} catch (SQLException exception) {
				throw new IllegalStateException("Could not read search result text.", exception);
			}
		}
		return value.toString();
	}

	private Double asDouble(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		return Double.valueOf(value.toString());
	}
}
