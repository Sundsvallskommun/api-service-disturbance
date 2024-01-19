package se.sundsvall.disturbance;

import static org.springframework.boot.SpringApplication.run;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import se.sundsvall.dept44.ServiceApplication;

@EnableFeignClients
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
@ServiceApplication
public class Application {
	public static void main(String... args) {
		run(Application.class, args);
	}
}
