package com.coworking.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CoworkingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoworkingApiApplication.class, args);
	}
}
