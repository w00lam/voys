package com.voys.search.application;

import java.util.List;
import java.util.UUID;

public interface SearchRepository {
	List<SearchResult> search(UUID ownerId, String query, int limit);
}
