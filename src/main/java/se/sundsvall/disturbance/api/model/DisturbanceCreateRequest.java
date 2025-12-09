package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Schema(description = "Disturbance create request model")
public class DisturbanceCreateRequest {

	@Schema(description = "Disturbance ID", examples = "435553", requiredMode = REQUIRED)
	@NotNull
	@Size(max = 255)
	private String id;

	@Schema(implementation = Category.class, requiredMode = REQUIRED)
	@NotNull
	private Category category;

	@Schema(description = "Title", examples = "Disturbance", requiredMode = REQUIRED)
	@NotNull
	@Size(max = 255)
	private String title;

	@Schema(description = "Description", examples = "Major disturbance", requiredMode = REQUIRED)
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DisturbanceCreateRequest that = (DisturbanceCreateRequest) o;
		return Objects.equals(id, that.id) && category == that.category && Objects.equals(title, that.title) && Objects.equals(description, that.description) && status == that.status && Objects.equals(plannedStartDate, that.plannedStartDate) && Objects
			.equals(plannedStopDate, that.plannedStopDate) && Objects.equals(affecteds, that.affecteds);
	}

	@Override
	public String toString() {
		return "DisturbanceCreateRequest{" +
			"id='" + id + '\'' +
			", category=" + category +
			", title='" + title + '\'' +
			", description='" + description + '\'' +
			", status=" + status +
			", plannedStartDate=" + plannedStartDate +
			", plannedStopDate=" + plannedStopDate +
			", affecteds=" + affecteds +
			'}';
	}
}
