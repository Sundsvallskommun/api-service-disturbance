package se.sundsvall.disturbance.integration.db.model;

import static java.util.Objects.isNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.sundsvall.disturbance.api.model.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class SubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "party_id", nullable = false)
	private String partyId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<SubscriptionOptOutEntity> optOuts;

	public void addOptOut(SubscriptionOptOutEntity optOut) {
		if(isNull(this.optOuts)) {
			this.optOuts = new HashSet<>();
		}
		optOuts.add(optOut);
	}

	public Long getId() {
		return id;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Set<SubscriptionOptOutEntity> getOptOuts() {
		return optOuts;
	}

	public void setOptOuts(Set<SubscriptionOptOutEntity> optOuts) {
		this.optOuts = optOuts;
	}

	public SubscriptionEntity withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public SubscriptionEntity withOptOuts(Set<SubscriptionOptOutEntity> optOuts) {
		this.optOuts = optOuts;
		return this;
	}
}
