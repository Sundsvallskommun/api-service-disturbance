package se.sundsvall.disturbance.integration.db.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import se.sundsvall.disturbance.api.model.Category;

@Entity
@Table(name = "opt_out_settings")
public class OptOutSettingsEntity implements Serializable {

	private static final long serialVersionUID = -6411620272543678958L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "category", nullable = false)
	private Category category;

	// E.g. Key: "facilityId", Value: "123456"
	@CollectionTable(
		name = "opt_out_settings_key_values",
		joinColumns = @JoinColumn(
			name = "opt_out_settings_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_opt_out_settings_key_values_opt_out_settings_id")))
	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, String> optOuts;

	public static OptOutSettingsEntity create() {
		return new OptOutSettingsEntity();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public OptOutSettingsEntity withId(Long id) {
		this.setId(id);
		return this;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public OptOutSettingsEntity withCategory(Category category) {
		this.setCategory(category);
		return this;
	}

	public Map<String, String> getOptOuts() {
		return optOuts;
	}

	public void setOptOuts(Map<String, String> optOuts) {
		this.optOuts = optOuts;
	}

	public OptOutSettingsEntity withOptOuts(Map<String, String> optOuts) {
		this.setOptOuts(optOuts);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, id, optOuts);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final OptOutSettingsEntity other)) { return false; }
		return (category == other.category) && Objects.equals(id, other.id) && Objects.equals(optOuts, other.optOuts);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("OptOutSettingsEntity [id=").append(id).append(", category=").append(category).append(", optOuts=").append(optOuts).append("]");
		return builder.toString();
	}
}
