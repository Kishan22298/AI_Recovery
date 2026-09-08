package com.recovery.ReceivablesGuard;

import org.springframework.boot.SpringApplication;

public class TestReceivablesGuardApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReceivablesGuardApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
