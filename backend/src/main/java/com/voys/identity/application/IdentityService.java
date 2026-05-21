package com.voys.identity.application;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voys.identity.domain.DuplicateEmailException;
import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.identity.infrastructure.persistence.UserAccountRepository;

@Service
public class IdentityService {

	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;

	public IdentityService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserAccount signUp(String email, String displayName, String password) {
		String normalizedEmail = normalizeEmail(email);
		if (userAccountRepository.existsByEmail(normalizedEmail)) {
			throw new DuplicateEmailException(normalizedEmail);
		}

		UserAccount account = UserAccount.create(
			normalizedEmail,
			displayName.trim(),
			passwordEncoder.encode(password)
		);

		return userAccountRepository.save(account);
	}

	public static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
