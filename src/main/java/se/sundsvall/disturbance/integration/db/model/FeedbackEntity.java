package se.sundsvall.disturbance.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "feedback",
	indexes = {
		@Index(name = "party_id_index", columnList = "party_id")
	})
public class FeedbackEntity implements Serializable {

	private static final long serialVersionUID = 6378788262069529085L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "party_id", nullable = false)
	private String partyId;

	@Column(name = "created")
	private OffsetDateTime created;

	@PrePersist
	void prePersist() {
		created = now(ZoneId.systemDefault()).truncatedTo(MILLIS);
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(final OffsetDateTime created) {
		this.created = created;
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, id, partyId);
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
		final FeedbackEntity other = (FeedbackEntity) obj;
		return Objects.equals(created, other.created) && Objects.equals(id, other.id) && Objects.equals(partyId, other.partyId);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("FeedbackEntity [id=").append(id).append(", partyId=").append(partyId).append(", created=").append(created).append("]");
		return builder.toString();
	}
}
