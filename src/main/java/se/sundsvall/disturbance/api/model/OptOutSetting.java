package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Map;
import java.util.Objects;

import se.sundsvall.disturbance.api.validation.OneOf;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Opt-out setting model")
public class OptOutSetting {

	@Schema(requiredMode = REQUIRED, description = "Category of the disturbance", example = "ELECTRICITY")
	@OneOf({"COMMUNICATION", "DISTRICT_COOLING", "DISTRICT_HEATING", "ELECTRICITY", "ELECTRICITY_TRADE", "WASTE_MANAGEMENT", "WATER"})
	private String category;

	@Schema(description = """
		Key/value pairs of opt-out values. E.g. ["facilityId" : "12345"].
		If multiple entries are added, they will have an "and"-relation. I.e. all properties must match in order for the opt-out to be evaluated as true.""",
		implementation = Map.class,
		example = "{\"facilityId\": \"123456\"}")
	private Map<@NotBlank String, @NotBlank String> values;

	public static OptOutSetting create() {
		return new OptOutSetting();
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public OptOutSetting withCategory(String category) {
		this.category = category;
		return this;
	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public OptOutSetting withValues(Map<String, String> values) {
		this.values = values;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, values);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OptOutSetting that = (OptOutSetting) o;
		return Objects.equals(category, that.category) && Objects.equals(values, that.values);
	}

	@Override
	public String toString() {
		return "OptOutSetting{" +
				"category='" + category + '\'' +
				", values=" + values +
				'}';
	}
}
