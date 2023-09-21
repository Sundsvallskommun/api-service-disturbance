package se.sundsvall.disturbance.api.model;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(description = "Subscription update request model")
public class SubscriptionUpdateRequest {

	@Schema(description = "Opt-out settings")
	private List<@Valid OptOutSetting> optOutSettings;

	public static SubscriptionUpdateRequest create() {
		return new SubscriptionUpdateRequest();
	}

	public List<OptOutSetting> getOptOutSettings() {
		return optOutSettings;
	}

	public void setOptOutSettings(List<OptOutSetting> optOutSettings) {
		this.optOutSettings = optOutSettings;
	}

	public SubscriptionUpdateRequest withOptOutSettings(List<OptOutSetting> optOutSettings) {
		this.optOutSettings = optOutSettings;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(optOutSettings);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final SubscriptionUpdateRequest other)) { return false; }
		return Objects.equals(optOutSettings, other.optOutSettings);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubscriptionUpdateRequest [optOutSettings=").append(optOutSettings).append("]");
		return builder.toString();
	}
}
