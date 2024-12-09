package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Schema(description = "Disturbance update request model")
public class DisturbanceUpdateRequest {

	@Schema(description = "Title", example = "Disturbance", requiredMode = REQUIRED)
	@Size(max = 255)
	private String title;

	@Schema(description = "Description", example = "Major disturbance")
	@Size(max = 8192)
	private String description;

	@Schema(implementation = Status.class)
	private Status status;

	@Schema(description = "Planned start date for the disturbance")
	private OffsetDateTime plannedStartDate;

	@Schema(description = "Planned stop date for the disturbance")
	private OffsetDateTime plannedStopDate;

	@ArraySchema(schema = @Schema(implementation = Affected.class))
	private List<@Valid Affected> affecteds;

	public static DisturbanceUpdateRequest create() {
		return new DisturbanceUpdateRequest();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public DisturbanceUpdateRequest withTitle(final String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public DisturbanceUpdateRequest withDescription(final String description) {
		this.description = description;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(final Status status) {
		this.status = status;
	}

	public DisturbanceUpdateRequest withStatus(final Status status) {
		this.status = status;
		return this;
	}

	public OffsetDateTime getPlannedStartDate() {
		return plannedStartDate;
	}

	public void setPlannedStartDate(final OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
	}

	public DisturbanceUpdateRequest withPlannedStartDate(final OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
		return this;
	}

	public OffsetDateTime getPlannedStopDate() {
		return plannedStopDate;
	}

	public void setPlannedStopDate(final OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
	}

	public DisturbanceUpdateRequest withPlannedStopDate(final OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
		return this;
	}

	public List<Affected> getAffecteds() {
		return affecteds;
	}

	public void setAffecteds(final List<Affected> affecteds) {
		this.affecteds = affecteds;
	}

	public DisturbanceUpdateRequest withAffecteds(final List<Affected> affecteds) {
		this.affecteds = affecteds;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, affecteds, plannedStartDate, plannedStopDate, status, title);
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
		final var other = (DisturbanceUpdateRequest) obj;
		return Objects.equals(description, other.description) && Objects.equals(affecteds, other.affecteds) && Objects.equals(plannedStartDate, other.plannedStartDate)
			&& Objects.equals(plannedStopDate, other.plannedStopDate) && status == other.status && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DisturbanceUpdateRequest [title=").append(title).append(", description=").append(description).append(", status=").append(status)
			.append(", plannedStartDate=").append(plannedStartDate).append(", plannedStopDate=").append(plannedStopDate).append(", affecteds=").append(affecteds).append("]");
		return builder.toString();
	}
}
