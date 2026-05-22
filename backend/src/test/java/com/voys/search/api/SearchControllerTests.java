package com.voys.search.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.voys.identity.application.UserPrincipal;
import com.voys.search.application.SearchResult;
import com.voys.search.application.SearchService;

class SearchControllerTests {

	private final UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private final SearchService searchService = mock(SearchService.class);
	private final SearchController controller = new SearchController(searchService);

	@Test
	void searchUsesAuthenticatedUserAndQueryParameter() {
		UserPrincipal principal = new UserPrincipal(ownerId, "user@example.com", "Voys User", "hash");
		SearchResult result = new SearchResult(
			"33333333-3333-3333-3333-333333333333",
			"Product strategy sync",
			"TITLE",
			"Product strategy sync",
			"COMPLETED",
			null
		);
		when(searchService.search(ownerId, "strategy")).thenReturn(List.of(result));

		List<SearchResult> response = controller.search(principal, "strategy");

		assertThat(response).containsExactly(result);
		verify(searchService).search(ownerId, "strategy");
	}
}
