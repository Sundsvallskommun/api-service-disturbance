package se.sundsvall.disturbance.integration.db.model;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import se.sundsvall.disturbance.api.model.Category;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_opt_out")
public class SubscriptionOptOutEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "category", nullable = false)
	@Enumerated(EnumType.STRING)
	private Category category;

	//Key: e.g. "facilityId", Value: e.g. "123456"
	@ElementCollection
	private Map<String, String> optOuts;

	public void addOptOut(String key, String value) {
		if(isNull(optOuts)) {
			this.optOuts = new HashMap<>();
		}
		optOuts.put(key, value);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Map<String, String> getOptOuts() {
		return optOuts;
	}

	public void setOptOuts(Map<String, String> optOuts) {
		this.optOuts = optOuts;
	}

	public SubscriptionOptOutEntity withId(Long id) {
		this.id = id;
		return this;
	}

	public SubscriptionOptOutEntity withCategory(Category category) {
		this.category = category;
		return this;
	}

	public SubscriptionOptOutEntity withOptOuts(Map<String, String> optOuts) {
		this.optOuts = optOuts;
		return this;
	}
}
