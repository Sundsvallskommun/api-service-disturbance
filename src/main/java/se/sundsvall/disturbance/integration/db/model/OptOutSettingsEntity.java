package se.sundsvall.disturbance.integration.db.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import se.sundsvall.disturbance.api.model.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "opt_out_settings")
public class OptOutSettingsEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "category", nullable = false)
	@Enumerated(EnumType.STRING)
	private Category category;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "subscription_id", foreignKey = @ForeignKey(name = "fk_opt_out_settings_subscription_id"))
	private SubscriptionEntity subscriptionEntity;

	//e.g. Key: "facilityId", Value: "123456"
	@CollectionTable(
			name = "opt_out_settings_key_values",
			joinColumns = @JoinColumn(
					name = "opt_out_settings_id",
					referencedColumnName = "id",
					foreignKey = @ForeignKey(name = "fk_opt_out_settings_opt_out_values")
			)
	)
	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, String> optOuts;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SubscriptionEntity getSubscriptionEntity() {
		return subscriptionEntity;
	}

	public void setSubscriptionEntity(SubscriptionEntity subscriptionEntity) {
		this.subscriptionEntity = subscriptionEntity;
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

	public OptOutSettingsEntity withCategory(Category category) {
		this.category = category;
		return this;
	}

	public OptOutSettingsEntity withOptOuts(Map<String, String> optOuts) {
		this.optOuts = optOuts;
		return this;
	}

	public OptOutSettingsEntity withSubscriptionEntity(SubscriptionEntity subscriptionEntity) {
		this.subscriptionEntity = subscriptionEntity;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OptOutSettingsEntity that = (OptOutSettingsEntity) o;
		return Objects.equals(id, that.id) && category == that.category && Objects.equals(subscriptionEntity, that.subscriptionEntity) && Objects.equals(optOuts, that.optOuts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, category, subscriptionEntity, optOuts);
	}

	@Override
	public String toString() {
		return "OptOutSettingsEntity{" +
				"id=" + id +
				", category=" + category +
				", subscriptionEntity=" + subscriptionEntity +
				", optOuts=" + optOuts +
				'}';
	}
}
