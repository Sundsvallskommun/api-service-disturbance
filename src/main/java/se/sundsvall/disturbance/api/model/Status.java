package se.sundsvall.disturbance.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status model", enumAsRef = true)
public enum Status {
	OPEN,
	CLOSED,
	PLANNED
}
