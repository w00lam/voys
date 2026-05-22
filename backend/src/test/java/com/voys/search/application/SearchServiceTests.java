package com.voys.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.voys.search.domain.InvalidSearchQueryException;

class SearchServiceTests {

	private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final UUID memoId = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private final SearchRepository searchRepository = mock(SearchRepository.class);
	private final SearchService searchService = new SearchService(searchRepository);

	@Test
	void searchNormalizesQueryAndReturnsTitleAndTranscriptMatchesForOwner() {
		when(searchRepository.search(ownerId, "strategy", 20)).thenReturn(List.of(
			new SearchResult(memoId.toString(), "Product strategy sync", "TITLE", "Product strategy sync", "COMPLETED", null),
			new SearchResult(memoId.toString(), "Lecture memo", "TRANSCRIPT", "strategy and roadmap", "COMPLETED", 42.5)
		));

		List<SearchResult> results = searchService.search(ownerId, "  Strategy  ");

		assertThat(results).hasSize(2);
		assertThat(results).extracting(SearchResult::matchType)
			.containsExactly("TITLE", "TRANSCRIPT");
		assertThat(results).extracting(SearchResult::snippet)
			.containsExactly("Product strategy sync", "strategy and roadmap");
		assertThat(results).extracting(SearchResult::segmentStartSeconds)
			.containsExactly(null, 42.5);
		verify(searchRepository).search(ownerId, "strategy", 20);
	}

	@Test
	void searchRejectsBlankQueryWithoutCallingRepository() {
		assertThatThrownBy(() -> searchService.search(ownerId, "   "))
			.isInstanceOf(InvalidSearchQueryException.class);

		verifyNoInteractions(searchRepository);
	}
}
