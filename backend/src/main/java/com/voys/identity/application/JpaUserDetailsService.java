package com.voys.identity.application;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voys.identity.infrastructure.persistence.UserAccount;
import com.voys.identity.infrastructure.persistence.UserAccountRepository;

@Service
public class JpaUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccountRepository;

	public JpaUserDetailsService(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		String email = IdentityService.normalizeEmail(username);
		UserAccount account = userAccountRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("User was not found."));

		return new UserPrincipal(
			account.getId(),
			account.getEmail(),
			account.getDisplayName(),
			account.getPasswordHash()
		);
	}
}
