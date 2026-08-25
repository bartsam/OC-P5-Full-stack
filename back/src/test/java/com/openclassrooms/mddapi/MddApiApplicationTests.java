package com.openclassrooms.mddapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class MddApiApplicationTests {

	@Container
	@ServiceConnection
	static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.4");

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.cors.allowed-origins}")
	private String corsAllowedOrigins;

	@Test
	void contextLoads() {
		assertThat(jwtSecret).isEqualTo("my-super-secret-key-for-testing-purposes-only");
		assertThat(corsAllowedOrigins).isEqualTo("http://localhost:4200");
	}

}
