package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Disturbance create request model")
public class DisturbanceCreateRequest {

	@Schema(description = "Disturbance ID", example = "435553", requiredMode = REQUIRED)
	@NotNull
	@Size(max = 255)
	private String id;

	@Schema(implementation = Category.class, requiredMode = REQUIRED)
	@NotNull
	private Category category;

	@Schema(description = "Title", example = "Disturbance", requiredMode = REQUIRED)
	@NotNull
	@Size(max = 255)
	private String title;

	@Schema(description = "Description", example = "Major disturbance", requiredMode = REQUIRED)
	@NotNull
	@Size(max = 8192)
	private String description;

	@Schema(implementation = Status.class, requiredMode = REQUIRED)
	@NotNull
	private Status status;

	@Schema(description = "Planned start date for the disturbance")
	private OffsetDateTime plannedStartDate;

	@Schema(description = "Planned stop date for the disturbance")
	private OffsetDateTime plannedStopDate;

	@ArraySchema(schema = @Schema(implementation = Affected.class))
	private List<@Valid Affected> affecteds;

	public static DisturbanceCreateRequest create() {
		return new DisturbanceCreateRequest();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public DisturbanceCreateRequest withId(final String id) {
		this.id = id;
		return this;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(final Category category) {
		this.category = category;
	}

	public DisturbanceCreateRequest withCategory(final Category category) {
		this.category = category;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public DisturbanceCreateRequest withTitle(final String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public DisturbanceCreateRequest withDescription(final String description) {
		this.description = description;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public DisturbanceCreateRequest withStatus(final Status status) {
		this.status = status;
		return this;
	}

	public OffsetDateTime getPlannedStartDate() {
		return plannedStartDate;
	}

	public void setPlannedStartDate(final OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
	}

	public DisturbanceCreateRequest withPlannedStartDate(final OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
		return this;
	}

	public OffsetDateTime getPlannedStopDate() {
		return plannedStopDate;
	}

	public void setPlannedStopDate(final OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
	}

	public DisturbanceCreateRequest withPlannedStopDate(final OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
		return this;
	}

	public List<Affected> getAffecteds() {
		return affecteds;
	}

	public void setAffecteds(final List<Affected> affecteds) {
		this.affecteds = affecteds;
	}

	public DisturbanceCreateRequest withAffecteds(final List<Affected> affecteds) {
		this.affecteds = affecteds;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, description, id, affecteds, plannedStartDate, plannedStopDate, status, title);
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
		final var other = (DisturbanceCreateRequest) obj;
		return category == other.category && Objects.equals(description, other.description) && Objects.equals(id, other.id) && Objects.equals(affecteds, other.affecteds)
			&& Objects.equals(plannedStartDate, other.plannedStartDate) && Objects.equals(plannedStopDate, other.plannedStopDate) && status == other.status
			&& Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DisturbanceCreateRequest [id=").append(id).append(", category=").append(category).append(", title=").append(title).append(", description=")
			.append(description).append(", status=").append(status).append(", plannedStartDate=").append(plannedStartDate).append(", plannedStopDate=").append(plannedStopDate)
			.append(", affecteds=").append(affecteds).append("]");
		return builder.toString();
	}
}
