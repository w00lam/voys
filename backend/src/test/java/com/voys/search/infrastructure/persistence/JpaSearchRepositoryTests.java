package com.voys.search.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.memo.infrastructure.persistence.VoiceMemo;
import com.voys.search.application.SearchResult;
import com.voys.transcription.infrastructure.persistence.Transcript;
import com.voys.transcription.infrastructure.persistence.TranscriptSegment;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = {
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.open-in-view=false"
})
@Import(JpaSearchRepository.class)
class JpaSearchRepositoryTests {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private JpaSearchRepository searchRepository;

	@Test
	void searchMatchesOwnedMemoTitlesAndCompletedTranscriptTextOnly() {
		UserAccount owner = user("owner@example.com", "Owner");
		UserAccount otherOwner = user("other@example.com", "Other Owner");
		VoiceMemo titleMatch = completedMemo(owner, "Product strategy sync", "No keyword here.");
		VoiceMemo transcriptMatch = completedMemoWithSegments(
			owner,
			"Lecture memo",
			"We discussed strategy and launch risks.",
			segment(0, 0.0, 3.5, "Opening context"),
			segment(1, 42.5, 48.0, "strategy and launch risks")
		);
		completedMemo(otherOwner, "Other strategy memo", "Other user's strategy transcript.");
		pendingMemo(owner, "Draft memo", "strategy should not match until completed");
		entityManager.flush();
		entityManager.clear();

		var results = searchRepository.search(owner.getId(), "strategy", 20);

		assertThat(results)
			.extracting(SearchResult::memoId)
			.containsExactlyInAnyOrder(titleMatch.getId().toString(), transcriptMatch.getId().toString());
		assertThat(results)
			.extracting(SearchResult::matchType)
			.containsExactlyInAnyOrder("TITLE", "TRANSCRIPT");
		assertThat(results)
			.extracting(SearchResult::snippet)
			.contains("Product strategy sync", "strategy and launch risks");
		SearchResult transcriptResult = results.stream()
			.filter(result -> result.memoId().equals(transcriptMatch.getId().toString()))
			.findFirst()
			.orElseThrow();
		assertThat(transcriptResult.segmentStartSeconds()).isEqualTo(42.5);
		SearchResult titleResult = results.stream()
			.filter(result -> result.memoId().equals(titleMatch.getId().toString()))
			.findFirst()
			.orElseThrow();
		assertThat(titleResult.segmentStartSeconds()).isNull();
	}

	@Test
	void searchAppliesLimit() {
		UserAccount owner = user("limit@example.com", "Limit Owner");
		completedMemo(owner, "Strategy one", "strategy one transcript");
		completedMemo(owner, "Strategy two", "strategy two transcript");
		entityManager.flush();
		entityManager.clear();

		assertThat(searchRepository.search(owner.getId(), "strategy", 1)).hasSize(1);
	}

	private VoiceMemo completedMemo(UserAccount owner, String title, String transcriptText) {
		VoiceMemo memo = VoiceMemo.createUploaded(owner, title);
		memo.markTranscriptionCompleted();
		entityManager.persist(memo);
		entityManager.persist(Transcript.create(memo, transcriptText));
		return memo;
	}

	private VoiceMemo completedMemoWithSegments(
		UserAccount owner,
		String title,
		String transcriptText,
		SegmentFixture... segments
	) {
		VoiceMemo memo = VoiceMemo.createUploaded(owner, title);
		memo.markTranscriptionCompleted();
		entityManager.persist(memo);
		Transcript transcript = Transcript.create(memo, transcriptText);
		entityManager.persist(transcript);
		for (SegmentFixture segment : segments) {
			entityManager.persist(TranscriptSegment.create(
				transcript,
				segment.position(),
				segment.startSeconds(),
				segment.endSeconds(),
				segment.text()
			));
		}
		return memo;
	}

	private VoiceMemo pendingMemo(UserAccount owner, String title, String transcriptText) {
		VoiceMemo memo = VoiceMemo.createUploaded(owner, title);
		entityManager.persist(memo);
		entityManager.persist(Transcript.create(memo, transcriptText));
		return memo;
	}

	private UserAccount user(String email, String displayName) {
		UserAccount user = UserAccount.create(email, displayName, "hash");
		entityManager.persist(user);
		return user;
	}

	private SegmentFixture segment(int position, double startSeconds, double endSeconds, String text) {
		return new SegmentFixture(position, startSeconds, endSeconds, text);
	}

	private record SegmentFixture(
		int position,
		double startSeconds,
		double endSeconds,
		String text
	) {}
}
