package com.voys.memo.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.voys.identity.application.UserPrincipal;
import com.voys.memo.application.MemoLibraryService;

class MemoControllerTitleUpdateTests {

	private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID MEMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final MemoLibraryService memoLibraryService = mock(MemoLibraryService.class);
	private final MemoController controller = new MemoController(memoLibraryService);

	@Test
	void updateMemoTitleUsesAuthenticatedUserAndRequestTitle() {
		UserPrincipal principal = new UserPrincipal(OWNER_ID, "user@example.com", "Voys User", "hash");
		MemoLibraryService.UpdateMemoMetadataCommand request =
			new MemoLibraryService.UpdateMemoMetadataCommand("Product strategy sync", null);
		MemoLibraryService.MemoMetadataUpdateResult expected =
			new MemoLibraryService.MemoMetadataUpdateResult(MEMO_ID.toString(), "Product strategy sync", null);
		when(memoLibraryService.updateMetadata(OWNER_ID, MEMO_ID, request)).thenReturn(expected);

		MemoLibraryService.MemoMetadataUpdateResult response = controller.updateMemo(principal, MEMO_ID, request);

		assertThat(response).isEqualTo(expected);
		verify(memoLibraryService).updateMetadata(OWNER_ID, MEMO_ID, request);
	}
}
