package se.sundsvall.disturbance.integration.db.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription",
		indexes = {
				@Index(name = "party_id_index", columnList = "party_id")
		})
public class SubscriptionEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "party_id", nullable = false)
	private String partyId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "subscriptionEntity")
	private Set<OptOutSettingsEntity> optOuts;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Set<OptOutSettingsEntity> getOptOuts() {
		return optOuts;
	}

	public void setOptOuts(Set<OptOutSettingsEntity> optOuts) {
		this.optOuts = optOuts;
	}

	public SubscriptionEntity withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public SubscriptionEntity withOptOuts(Set<OptOutSettingsEntity> optOuts) {
		this.optOuts = optOuts;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, partyId, optOuts);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SubscriptionEntity entity = (SubscriptionEntity) o;
		return Objects.equals(id, entity.id) && Objects.equals(partyId, entity.partyId) && Objects.equals(optOuts, entity.optOuts);
	}

	@Override
	public String toString() {
		return "SubscriptionEntity{" +
				"id=" + id +
				", partyId='" + partyId + '\'' +
				", optOuts=" + optOuts +
				'}';
	}
}
