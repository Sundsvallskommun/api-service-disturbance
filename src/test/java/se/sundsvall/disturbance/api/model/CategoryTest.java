package se.sundsvall.disturbance.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.disturbance.api.model.Category.COMMUNICATION;
import static se.sundsvall.disturbance.api.model.Category.DISTRICT_COOLING;
import static se.sundsvall.disturbance.api.model.Category.DISTRICT_HEATING;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY_TRADE;
import static se.sundsvall.disturbance.api.model.Category.WASTE_MANAGEMENT;
import static se.sundsvall.disturbance.api.model.Category.WATER;

import org.junit.jupiter.api.Test;

class CategoryTest {

	@Test
	void categoryEnum() {
		System.out.println(COMMUNICATION.toString());
		assertThat(Category.values()).containsExactly(COMMUNICATION, DISTRICT_COOLING, DISTRICT_HEATING, ELECTRICITY, ELECTRICITY_TRADE, WASTE_MANAGEMENT, WATER);
	}
}
