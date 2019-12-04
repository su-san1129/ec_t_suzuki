package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EcprojectOneApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcprojectOneApplication.class, args);
	}

}