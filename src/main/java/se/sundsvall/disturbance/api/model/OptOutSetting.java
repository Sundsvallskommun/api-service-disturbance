package se.sundsvall.disturbance.api.model;

import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Opt out model")
public class OptOutSetting {

	@Schema(implementation = Category.class)
	private Category category;

	private Map<String, String> values;

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
