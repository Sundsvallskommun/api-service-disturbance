package se.sundsvall.disturbance.api.model;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(description = "Opt out model")
public class OptOutSetting {

	@Schema(implementation = Category.class)
	private Category category;

	@ArraySchema(schema = @Schema(implementation = OptOutValue.class))
	private List<@Valid OptOutValue> values;

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

	public List<OptOutValue> getValues() {
		return values;
	}

	public void setValues(List<OptOutValue> values) {
		this.values = values;
	}

	public OptOutSetting withValues(List<OptOutValue> values) {
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
