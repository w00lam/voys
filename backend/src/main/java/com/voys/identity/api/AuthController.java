package com.voys.identity.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.voys.identity.application.IdentityService;
import com.voys.identity.application.UserPrincipal;

@RestController
@RequestMapping("/api")
public class AuthController {

	private final IdentityService identityService;
	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();

	public AuthController(
		IdentityService identityService,
		AuthenticationManager authenticationManager,
		SecurityContextRepository securityContextRepository
	) {
		this.identityService = identityService;
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
	}

	@PostMapping("/auth/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse signUp(
		@Valid @RequestBody SignUpRequest request,
		HttpServletRequest httpRequest,
		HttpServletResponse httpResponse
	) {
		identityService.signUp(request.email(), request.displayName(), request.password());
		Authentication authentication = authenticate(request.email(), request.password(), httpRequest, httpResponse);
		return UserResponse.from((UserPrincipal) authentication.getPrincipal());
	}

	@PostMapping("/auth/login")
	public UserResponse login(
		@Valid @RequestBody LoginRequest request,
		HttpServletRequest httpRequest,
		HttpServletResponse httpResponse
	) {
		Authentication authentication = authenticate(request.email(), request.password(), httpRequest, httpResponse);
		return UserResponse.from((UserPrincipal) authentication.getPrincipal());
	}

	@PostMapping("/auth/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		logoutHandler.logout(request, response, authentication);
	}

	@GetMapping("/auth/csrf")
	public CsrfResponse csrf(CsrfToken token) {
		return new CsrfResponse(token.getHeaderName(), token.getParameterName(), token.getToken());
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
		return UserResponse.from(principal);
	}

	private Authentication authenticate(
		String email,
		String password,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		Authentication authentication = authenticationManager.authenticate(
			UsernamePasswordAuthenticationToken.unauthenticated(email, password)
		);
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);
		return authentication;
	}

	public record SignUpRequest(
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank @Size(max = 120) String displayName
	) {
	}

	public record LoginRequest(
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(min = 8, max = 72) String password
	) {
	}

	public record UserResponse(String id, String email, String displayName) {
		static UserResponse from(UserPrincipal principal) {
			return new UserResponse(principal.id().toString(), principal.email(), principal.displayName());
		}
	}

	public record CsrfResponse(String headerName, String parameterName, String token) {
	}
}
