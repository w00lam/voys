package com.voys;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.voys.shared.api.HealthController;

class BackendApplicationTests {

	@Test
	void healthEndpointReturnsOk() {
		HealthController controller = new HealthController();

		HealthController.HealthResponse response = controller.health();

		assertThat(response.status()).isEqualTo("ok");
		assertThat(response.timestamp()).isNotNull();
	}
}

