package com.voys.identity.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:auth-csrf;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class AuthControllerCsrfIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void signUpAcceptsCsrfTokenIssuedForBrowserSession() throws Exception {
		MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf"))
			.andExpect(status().isOk())
			.andReturn();
		AuthController.CsrfResponse csrf = objectMapper.readValue(
			csrfResult.getResponse().getContentAsString(),
			AuthController.CsrfResponse.class
		);
		Cookie xsrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");
		assertThat(xsrfCookie).isNotNull();
		assertThat(csrf.token()).isEqualTo(xsrfCookie.getValue());

		mockMvc.perform(post("/api/auth/signup")
				.cookie(xsrfCookie)
				.header(csrf.headerName(), csrf.token())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "csrf-user@example.com",
					  "password": "password123",
					  "displayName": "CSRF User"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value("csrf-user@example.com"))
			.andExpect(jsonPath("$.displayName").value("CSRF User"));
	}
}
