package se.sundsvall.disturbance.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category model", enumAsRef = true)
public enum Category {

	COMMUNICATION("COMMUNICATION"),
	DISTRICT_COOLING("DISTRICT_COOLING"),
	DISTRICT_HEATING("DISTRICT_HEATING"),
	ELECTRICITY("ELECTRICITY"),
	ELECTRICITY_TRADE("ELECTRICITY_TRADE"),
	WASTE_MANAGEMENT("WASTE_MANAGEMENT"),
	WATER("WATER");

	private final String value;

	Category(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static Category fromValue(String value) {
		for (Category category : Category.values()) {
			if (category.value.equalsIgnoreCase(value)) {
				return category;
			}
		}
		throw new IllegalArgumentException("Invalid category: Couldn't match: " + value + ", to a Category");
	}

	@Override
	public String toString() {
		return this.getValue();
	}
}
