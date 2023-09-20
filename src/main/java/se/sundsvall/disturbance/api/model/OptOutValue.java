package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Opt out value")
public class OptOutValue {

	@Schema(description = "Opt out value key", example = "faciltiyId", requiredMode = REQUIRED)
	@NotBlank
	private String key;

	@Schema(description = "Opt out value", example = "123456789", requiredMode = REQUIRED)
	@NotBlank
	private String value;

	public static OptOutValue create() {
		return new OptOutValue();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public OptOutValue withKey(String key) {
		this.key = key;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public OptOutValue withValue(String value) {
		this.value = value;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final OptOutValue other)) { return false; }
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("OptOutValue [key=").append(key).append(", value=").append(value).append("]");
		return builder.toString();
	}
}
