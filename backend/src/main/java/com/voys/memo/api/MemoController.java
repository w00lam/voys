package com.voys.memo.api;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.voys.identity.application.UserPrincipal;
import com.voys.memo.application.MemoLibraryService;
import com.voys.memo.application.MemoLibraryService.AudioDownload;
import com.voys.memo.application.MemoLibraryService.MemoDetail;
import com.voys.memo.application.MemoLibraryService.MemoSummary;

@RestController
public class MemoController {

	private final MemoLibraryService memoLibraryService;

	public MemoController(MemoLibraryService memoLibraryService) {
		this.memoLibraryService = memoLibraryService;
	}

	@GetMapping("/api/memos")
	public List<MemoSummary> listMemos(@AuthenticationPrincipal UserPrincipal principal) {
		return memoLibraryService.listMemos(principal.id());
	}

	@GetMapping("/api/memos/{memoId}")
	public MemoDetail getMemo(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		return memoLibraryService.getMemo(principal.id(), memoId);
	}

	@GetMapping("/api/memos/{memoId}/audio")
	public ResponseEntity<Resource> getAudio(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId
	) {
		AudioDownload audio = memoLibraryService.getAudio(principal.id(), memoId);
		MediaType mediaType = MediaType.parseMediaType(audio.contentType());

		return ResponseEntity.ok()
			.contentType(mediaType)
			.contentLength(audio.contentLength())
			.cacheControl(CacheControl.noStore())
			.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
			.body(audio.resource());
	}

	@PatchMapping("/api/memos/{memoId}")
	public MemoLibraryService.MemoTitleUpdateResult updateMemo(
		@AuthenticationPrincipal UserPrincipal principal,
		@PathVariable UUID memoId,
		@RequestBody MemoLibraryService.UpdateMemoTitleCommand request
	) {
		return memoLibraryService.updateTitle(principal.id(), memoId, request);
	}
}
