package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(description = "Subscription model", accessMode = READ_ONLY)
public class Subscription {

	@Schema(description = "Subscription ID", example = "1234")
	private Long id;

	@Schema(description = "PartyId (e.g. a personId or an organizationId)", example = "81471222-5798-11e9-ae24-57fa13b361e1")
	private String partyId;

	@Schema(description = "Opt out settings")
	private List<@Valid OptOutSetting> optOutSettings;

	@Schema(description = "Created timestamp")
	private OffsetDateTime created;

	@Schema(description = "Updated timestamp")
	private OffsetDateTime updated;

	public static Subscription create() {
		return new Subscription();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Subscription withId(Long id) {
		this.id = id;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Subscription withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public List<OptOutSetting> getOptOutSettings() {
		return optOutSettings;
	}

	public void setOptOutSettings(List<OptOutSetting> optOutSettings) {
		this.optOutSettings = optOutSettings;
	}

	public Subscription withOptOutSettings(List<OptOutSetting> optOutSettings) {
		this.optOutSettings = optOutSettings;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public Subscription withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public Subscription withUpdated(OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, id, optOutSettings, partyId, updated);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final Subscription other)) { return false; }
		return Objects.equals(created, other.created) && Objects.equals(id, other.id) && Objects.equals(optOutSettings, other.optOutSettings) && Objects.equals(partyId, other.partyId) && Objects.equals(updated, other.updated);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Subscription [id=").append(id).append(", partyId=").append(partyId).append(", optOutSettings=").append(optOutSettings).append(", created=").append(created).append(", updated=").append(updated).append("]");
		return builder.toString();
	}
}
