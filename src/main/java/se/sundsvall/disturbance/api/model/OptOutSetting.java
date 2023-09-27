package se.sundsvall.disturbance.api.model;

import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Opt-out setting model")
public class OptOutSetting {

	@Schema(implementation = Category.class)
	@NotNull
	private Category category;

	@Schema(description = """
		Key/value pairs of opt-out values. E.g. ["facilityId" : "12345"].
		If multiple entries are added, they will have an "and"-relation. I.e. all properties must match in order for the opt-out to be evaluated as true.""",
		implementation = Map.class,
		example = "{\"facilityId\": \"123456\"}")
	private Map<@NotBlank String, @NotBlank String> values;

	public static OptOutSetting create() {
		return new OptOutSetting();
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public OptOutSetting withCategory(Category category) {
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
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final OptOutSetting other)) { return false; }
		return (category == other.category) && Objects.equals(values, other.values);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("OptOutSetting [category=").append(category).append(", values=").append(values).append("]");
		return builder.toString();
	}
}
