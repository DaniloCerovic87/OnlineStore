package com.order.orderservice;

import org.springframework.boot.SpringApplication;

public class TestOrderserviceApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
