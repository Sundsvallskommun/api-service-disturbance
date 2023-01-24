package se.sundsvall.disturbance.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.service.DisturbanceFeedbackService;
import se.sundsvall.disturbance.service.DisturbanceService;

@RestController
@Validated
@RequestMapping("/disturbances")
@Tag(name = "Disturbance", description = "Disturbance operations")
public class DisturbanceResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisturbanceResource.class);

	@Autowired
	private DisturbanceService disturbanceService;

	@Autowired
	private DisturbanceFeedbackService disturbanceFeedbackService;

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	@Operation(summary = "Create a new disturbance.")
	@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation")
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> createDisturbance(UriComponentsBuilder uriComponentsBuilder, @RequestBody @Valid DisturbanceCreateRequest body) {
		LOGGER.debug("Received createDisturbance request: body='{}'", body);

		final var result = disturbanceService.createDisturbance(body);

		return created(uriComponentsBuilder.path("/disturbances/{category}/{disturbanceId}").buildAndExpand(result.getCategory(), result.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Return all disturbances filtered on status and category.")
	@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Disturbance.class))))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<List<Disturbance>> getDisturbances(
		@Parameter(name = "status", description = "Status filter parameter", required = false) @RequestParam(value = "status", required = false) List<Status> status,
		@Parameter(name = "category", description = "Category filter parameter", required = false) @RequestParam(value = "category", required = false) List<Category> category) {
		return ok(disturbanceService.findByStatusAndCategory(status, category));
	}

	@GetMapping(path = "/{category}/{disturbanceId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Return information about a specific disturbance.")
	@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Disturbance.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Disturbance> getDisturbance(
		@Parameter(name = "category", description = "Disturbance category", required = true) @PathVariable(name = "category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @PathVariable(name = "disturbanceId") String disturbanceId) {
		LOGGER.debug("Received getDisturbance request: category='{}'. disturbanceId='{}'", category, disturbanceId);

		return ok(disturbanceService.findByCategoryAndDisturbanceId(category, disturbanceId));
	}

	@GetMapping(path = "/affecteds/{partyId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Return all present disturbances for a person or an organization.")
	@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Disturbance.class))))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<List<Disturbance>> getDisturbancesByPartyId(
		@Parameter(name = "partyId", description = "PartyId (e.g. a personId or an organizationId)", required = true, example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @PathVariable(name = "partyId") String partyId,
		@Parameter(name = "status", description = "Status filter parameter", required = false) @RequestParam(value = "status", required = false) List<Status> status,
		@Parameter(name = "category", description = "Category filter parameter", required = false) @RequestParam(value = "category", required = false) List<Category> category) {
		LOGGER.debug("Received getDisturbancesByPartyId request: partyId='{}', status='{}', category='{}'", partyId, status, category);

		return ok(disturbanceService.findByPartyIdAndCategoryAndStatus(partyId, category, status));
	}

	@PatchMapping(path = "/{category}/{disturbanceId}", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Manage updates of a disturbance. Should be used when the set of affected persons/organizations is changed or the disturbance description is updated.")
	@ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Disturbance.class)))
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Disturbance> updateDisturbance(
		@Parameter(name = "category", description = "Disturbance category", required = true) @PathVariable(name = "category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @PathVariable(name = "disturbanceId") String disturbanceId,
		@RequestBody @Valid DisturbanceUpdateRequest body) {
		LOGGER.debug("Received updateDisturbance request: category='{}', disturbanceId='{}', body='{}'", category, disturbanceId, body);

		return ok(disturbanceService.updateDisturbance(category, disturbanceId, body));
	}

	@DeleteMapping(path = "/{category}/{disturbanceId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Delete a disturbance. Should be used when the disturbance is resolved. Any affected persons/organizations (with ordered feedback) will be notified of the resolved disturbance.")
	@ApiResponse(responseCode = "204", description = "Successful operation")
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> deleteDisturbance(
		@Parameter(name = "category", description = "Disturbance category", required = true) @PathVariable(name = "category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @PathVariable(name = "disturbanceId") String disturbanceId) {
		LOGGER.debug("Received deleteDisturbance request: category='{}', disturbanceId='{}'", category, disturbanceId);

		disturbanceService.deleteDisturbance(category, disturbanceId);

		return noContent().build();
	}

	@PostMapping(path = "/{category}/{disturbanceId}/feedback", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Create disturbance feedback for a person/organization. I.e. subscribe on notifications when the disturbance is updated or resolved.")
	@ApiResponse(responseCode = "204", description = "Successful operation")
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> createDisturbanceFeedback(
		@Parameter(name = "category", description = "Disturbance category", required = true) @PathVariable(name = "category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @PathVariable(name = "disturbanceId") String disturbanceId,
		@RequestBody @Valid DisturbanceFeedbackCreateRequest body) {
		LOGGER.debug("Received createDisturbanceFeedback request: category='{}', disturbanceId='{}', body='{}'", category, disturbanceId, body);

		disturbanceFeedbackService.createDisturbanceFeedback(category, disturbanceId, body);

		return noContent().build();
	}
}
