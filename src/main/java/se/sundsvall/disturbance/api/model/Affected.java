package se.sundsvall.disturbance.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Schema(description = "Affected persons and/or organizations model")
public class Affected {

	@Schema(description = "PartyId (e.g. a personId or an organizationId)", example = "81471222-5798-11e9-ae24-57fa13b361e1", requiredMode = REQUIRED)
	@ValidUuid
	private String partyId;

	@Schema(description = "Reference information", example = "Streetname 123")
	@NotNull
	@Size(max = 512)
	private String reference;

	@Schema(description = "Facitlity-ID. The unique facility identifier", example = "735999109175011012")
	private String facilityId;

	@Schema(description = "The coordinates to the facility on the format:{coordinate-system}:N{north-coordinate}:E{east-coordinate}", example = "SWEREF 991715:N6919620.98828125:E152414.77734375")
	private String coordinates;

	public static Affected create() {
		return new Affected();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public Affected withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(final String reference) {
		this.reference = reference;
	}

	public Affected withReference(final String reference) {
		this.reference = reference;
		return this;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(final String facilityId) {
		this.facilityId = facilityId;
	}

	public Affected withFacilityId(final String facilityId) {
		this.facilityId = facilityId;
		return this;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(final String coordinates) {
		this.coordinates = coordinates;
	}

	public Affected withCoordinates(final String coordinates) {
		this.coordinates = coordinates;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(facilityId, coordinates, partyId, reference);
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
		final var other = (Affected) obj;
		return Objects.equals(facilityId, other.facilityId) && Objects.equals(coordinates, other.coordinates) && Objects.equals(partyId, other.partyId) && Objects.equals(reference, other.reference);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("Affected [partyId=").append(partyId).append(", reference=").append(reference).append(", facilityId=").append(facilityId).append(", coordinates=").append(coordinates).append("]");
		return builder.toString();
	}
}
