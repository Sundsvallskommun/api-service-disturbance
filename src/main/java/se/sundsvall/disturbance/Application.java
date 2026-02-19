package se.sundsvall.disturbance;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.dept44.ServiceApplication;

import static org.springframework.boot.SpringApplication.run;

@EnableFeignClients
@EnableScheduling
@ServiceApplication
public class Application {
	public static void main(String... args) {
		run(Application.class, args);
	}
}
