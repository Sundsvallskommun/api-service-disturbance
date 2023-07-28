package se.sundsvall.disturbance;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.cloud.openfeign.EnableFeignClients;

import se.sundsvall.dept44.ServiceApplication;

@EnableFeignClients
@ServiceApplication
public class Application {
	public static void main(String... args) {
		run(Application.class, args);
	}
}
