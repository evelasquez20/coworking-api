package com.coworking.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Le indica a Spring Boot que cargue application-test.yml
class CoworkingApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
