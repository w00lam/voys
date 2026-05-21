package com.voys.search.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.voys.search.domain.InvalidSearchQueryException;

@Service
public class SearchService {

	private final SearchRepository searchRepository;

	public SearchService(SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}

	public List<SearchResult> search(UUID ownerId, String query) {
		if (query == null || query.trim().isEmpty()) {
			throw new InvalidSearchQueryException("Search query cannot be blank.");
		}

		String normalizedQuery = query.trim().toLowerCase();
		return searchRepository.search(ownerId, normalizedQuery, 20);
	}
}
