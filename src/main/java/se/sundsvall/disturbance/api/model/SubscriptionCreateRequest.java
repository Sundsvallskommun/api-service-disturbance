package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Schema(description = "Subscription create request model")
public class SubscriptionCreateRequest {

	@ValidUuid
	@Schema(description = "PartyId (e.g. a personId or an organizationId)", example = "81471222-5798-11e9-ae24-57fa13b361e1", requiredMode = REQUIRED)
	private String partyId;

	@Schema(description = "Opt-out settings")
	private List<@Valid OptOutSetting> optOutSettings;

	public static SubscriptionCreateRequest create() {
		return new SubscriptionCreateRequest();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public SubscriptionCreateRequest withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public List<OptOutSetting> getOptOutSettings() {
		return optOutSettings;
	}

	public void setOptOutSettings(List<OptOutSetting> optOutSettings) {
		this.optOutSettings = optOutSettings;
	}

	public SubscriptionCreateRequest withOptOutSettings(List<OptOutSetting> optOutSettings) {
		this.optOutSettings = optOutSettings;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(optOutSettings, partyId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final SubscriptionCreateRequest other)) { return false; }
		return Objects.equals(optOutSettings, other.optOutSettings) && Objects.equals(partyId, other.partyId);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubscriptionCreateRequest [partyId=").append(partyId).append(", optOutSettings=").append(optOutSettings).append("]");
		return builder.toString();
	}
}
