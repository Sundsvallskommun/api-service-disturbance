package se.sundsvall.disturbance.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;

@Entity
@Table(name = "subscription",
	indexes = {
		@Index(name = "party_id_index", columnList = "party_id"),
		@Index(name = "municipality_id_index", columnList = "municipality_id")
	})
public class SubscriptionEntity implements Serializable {

	private static final long serialVersionUID = -8971474792184802872L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "municipality_id", nullable = false)
	private String municipalityId;

	@Column(name = "party_id", nullable = false)
	private String partyId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "subscription_id", foreignKey = @ForeignKey(name = "fk_opt_out_settings_subscription_id"))
	private List<OptOutSettingsEntity> optOutSettings;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	public static SubscriptionEntity create() {
		return new SubscriptionEntity();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SubscriptionEntity withId(Long id) {
		this.setId(id);
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public SubscriptionEntity withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public SubscriptionEntity withPartyId(String partyId) {
		this.setPartyId(partyId);
		return this;
	}

	public List<OptOutSettingsEntity> getOptOutSettings() {
		return optOutSettings;
	}

	public void setOptOutSettings(List<OptOutSettingsEntity> optOutSettings) {
		if (this.optOutSettings == null) {
			this.optOutSettings = new ArrayList<>();
		}

		if (nonNull(optOutSettings)) {
			this.optOutSettings.retainAll(optOutSettings);
			this.optOutSettings.addAll(optOutSettings);
		} else {
			this.optOutSettings.clear();
		}
	}

	public SubscriptionEntity withOptOutSettings(List<OptOutSettingsEntity> optOutSettings) {
		this.setOptOutSettings(optOutSettings);
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public SubscriptionEntity withCreated(OffsetDateTime created) {
		this.setCreated(created);
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public SubscriptionEntity withUpdated(OffsetDateTime updated) {
		this.setUpdated(updated);
		return this;
	}

	@PrePersist
	void prePersist() {
		this.created = now(systemDefault()).truncatedTo(MILLIS);
	}

	@PreUpdate
	void preUpdate() {
		this.updated = now(systemDefault()).truncatedTo(MILLIS);
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, id, municipalityId, optOutSettings, partyId, updated);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final SubscriptionEntity other)) { return false; }
		return Objects.equals(created, other.created) && Objects.equals(id, other.id) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(optOutSettings, other.optOutSettings) && Objects.equals(partyId, other.partyId) && Objects
			.equals(updated, other.updated);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubscriptionEntity [id=").append(id).append(", municipalityId=").append(municipalityId).append(", partyId=").append(partyId).append(", optOutSettings=").append(optOutSettings).append(", created=").append(created).append(
			", updated=").append(updated).append("]");
		return builder.toString();
	}
}
