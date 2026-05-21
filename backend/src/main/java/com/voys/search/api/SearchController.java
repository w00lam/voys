package com.voys.search.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voys.identity.application.UserPrincipal;
import com.voys.search.application.SearchResult;
import com.voys.search.application.SearchService;

@RestController
public class SearchController {

	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping("/api/search")
	public List<SearchResult> search(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestParam("q") String query
	) {
		return searchService.search(principal.id(), query);
	}
}
