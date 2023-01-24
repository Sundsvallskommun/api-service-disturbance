package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Schema(description = "Disturbance feedback request model")
public class DisturbanceFeedbackCreateRequest {

	@ValidUuid
	@Schema(description = "PartyId (e.g. a personId or an organizationId)", example = "81471222-5798-11e9-ae24-57fa13b361e1", requiredMode = REQUIRED)
	private String partyId;

	public static DisturbanceFeedbackCreateRequest create() {
		return new DisturbanceFeedbackCreateRequest();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public DisturbanceFeedbackCreateRequest withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (DisturbanceFeedbackCreateRequest) obj;
		return Objects.equals(partyId, other.partyId);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DisturbanceFeedbackCreateRequest [partyId=")
			.append(partyId).append("]");
		return builder.toString();
	}
}
