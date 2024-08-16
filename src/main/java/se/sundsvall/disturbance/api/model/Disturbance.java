package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Disturbance model")
public class Disturbance {

	@Schema(description = "Disturbance ID", example = "435553")
	private String id;

	@Schema(description = "Municipality ID", example = "2281", accessMode = READ_ONLY)
	private String municipalityId;

	@Schema(implementation = Category.class)
	@NotNull
	private Category category;

	@Schema(implementation = Status.class)
	@NotNull
	private Status status;

	@Schema(description = "Title", example = "Disturbance")
	private String title;

	@Schema(description = "Description", example = "Major disturbance in city")
	private String description;

	@Schema(description = "Planned start date for the disturbance")
	private OffsetDateTime plannedStartDate;

	@Schema(description = "Planned stop date for the disturbance")
	private OffsetDateTime plannedStopDate;

	@Schema(description = "Created timestamp", accessMode = READ_ONLY)
	private OffsetDateTime created;

	@Schema(description = "Updated timestamp", accessMode = READ_ONLY)
	private OffsetDateTime updated;

	@ArraySchema(schema = @Schema(implementation = Affected.class))
	private List<Affected> affecteds;

	public static Disturbance create() {
		return new Disturbance();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Disturbance withId(String id) {
		this.id = id;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public Disturbance withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Disturbance withCategory(Category category) {
		this.category = category;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Disturbance withTitle(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Disturbance withDescription(String description) {
		this.description = description;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Disturbance withStatus(Status status) {
		this.status = status;
		return this;
	}

	public OffsetDateTime getPlannedStartDate() {
		return plannedStartDate;
	}

	public void setPlannedStartDate(OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
	}

	public Disturbance withPlannedStartDate(OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
		return this;
	}

	public OffsetDateTime getPlannedStopDate() {
		return plannedStopDate;
	}

	public void setPlannedStopDate(OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
	}

	public Disturbance withPlannedStopDate(OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
		return this;
	}

	public List<Affected> getAffecteds() {
		return affecteds;
	}

	public void setAffecteds(List<Affected> affecteds) {
		this.affecteds = affecteds;
	}

	public Disturbance withAffecteds(List<Affected> affecteds) {
		this.affecteds = affecteds;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public Disturbance withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public Disturbance withUpdated(OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(affecteds, category, created, description, id, municipalityId, plannedStartDate, plannedStopDate, status, title, updated);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final Disturbance other)) { return false; }
		return Objects.equals(affecteds, other.affecteds) && (category == other.category) && Objects.equals(created, other.created) && Objects.equals(description, other.description) && Objects.equals(id, other.id) && Objects.equals(municipalityId,
			other.municipalityId) && Objects.equals(plannedStartDate, other.plannedStartDate) && Objects.equals(plannedStopDate, other.plannedStopDate) && (status == other.status) && Objects.equals(title, other.title) && Objects.equals(updated,
				other.updated);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Disturbance [id=").append(id).append(", municipalityId=").append(municipalityId).append(", category=").append(category).append(", status=").append(status).append(", title=").append(title).append(", description=").append(description)
			.append(", plannedStartDate=").append(plannedStartDate).append(", plannedStopDate=").append(plannedStopDate).append(", created=").append(created).append(", updated=").append(updated).append(", affecteds=").append(affecteds).append("]");
		return builder.toString();
	}
}
