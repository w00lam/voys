package com.voys.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.voys.identity.domain.DuplicateEmailException;
import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.identity.infrastructure.persistence.UserAccountRepository;

class IdentityServiceTests {

	private final UserAccountRepository repository = org.mockito.Mockito.mock(UserAccountRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final IdentityService identityService = new IdentityService(repository, passwordEncoder);

	@Test
	void signUpNormalizesEmailAndHashesPassword() {
		when(repository.existsByEmail("user@example.com")).thenReturn(false);
		when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UserAccount account = identityService.signUp(" User@Example.COM ", "User", "password123");

		assertThat(account.getEmail()).isEqualTo("user@example.com");
		assertThat(account.getDisplayName()).isEqualTo("User");
		assertThat(account.getPasswordHash()).isNotEqualTo("password123");
		assertThat(passwordEncoder.matches("password123", account.getPasswordHash())).isTrue();
		verify(repository).save(any(UserAccount.class));
	}

	@Test
	void signUpRejectsDuplicateEmail() {
		when(repository.existsByEmail("user@example.com")).thenReturn(true);

		assertThatThrownBy(() -> identityService.signUp("user@example.com", "User", "password123"))
			.isInstanceOf(DuplicateEmailException.class);
	}
}
