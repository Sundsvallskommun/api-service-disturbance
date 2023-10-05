package se.sundsvall.disturbance.integration.messaging.mapper;

/**
 * To be used with messagings filters.
 * No need to translate from string value to enum, hence no e.g. fromValue method.
 */
public enum Filter {
	CATEGORY("category"),
	FACILITY_ID("facilityId"),
	TYPE("type");

	private final String value;

	Filter(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
