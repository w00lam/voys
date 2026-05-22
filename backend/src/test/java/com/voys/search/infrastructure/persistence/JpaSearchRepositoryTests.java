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
		VoiceMemo transcriptMatch = completedMemo(owner, "Lecture memo", "We discussed strategy and launch risks.");
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
			.contains("Product strategy sync", "We discussed strategy and launch risks.");
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
}
