package se.sundsvall.disturbance.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.service.DisturbanceService;

@RestController
@Validated
@RequestMapping("/{municipalityId}/disturbances")
@Tag(name = "Disturbance", description = "Disturbance operations")
class DisturbanceResource {

	private final DisturbanceService disturbanceService;

	DisturbanceResource(DisturbanceService disturbanceService) {
		this.disturbanceService = disturbanceService;
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a new disturbance.", responses = {
		@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, description = "Location of the created resource.", schema = @Schema(type = "string")), description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> createDisturbance(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281", required = true) @ValidMunicipalityId @PathVariable final String municipalityId,
		@RequestBody @Valid final DisturbanceCreateRequest body) {

		final var result = disturbanceService.createDisturbance(municipalityId, body);
		return created(fromPath("/{municipalityId}/disturbances/{category}/{disturbanceId}").buildAndExpand(municipalityId, result.getCategory(), result.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Return all disturbances filtered on status and category.", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<List<Disturbance>> getDisturbances(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281", required = true) @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "status", description = "Status filter parameter") @RequestParam(value = "status", required = false) final List<Status> status,
		@Parameter(name = "category", description = "Category filter parameter") @RequestParam(value = "category", required = false) final List<Category> category) {

		return ok(disturbanceService.findByMunicipalityIdAndStatusAndCategory(municipalityId, status, category));
	}

	@GetMapping(path = "/{category}/{disturbanceId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Return information about a specific disturbance.", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Disturbance> getDisturbance(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281", required = true) @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "category", description = "Disturbance category", required = true) @PathVariable(name = "category") final Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @PathVariable(name = "disturbanceId") final String disturbanceId) {

		return ok(disturbanceService.findByMunicipalityIdAndCategoryAndDisturbanceId(municipalityId, category, disturbanceId));
	}

	@GetMapping(path = "/affecteds/{partyId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Return all present disturbances for a person or an organization.", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<List<Disturbance>> getDisturbancesByPartyId(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281", required = true) @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "PartyId (e.g. a personId or an organizationId)", required = true, example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @PathVariable(name = "partyId") final String partyId,
		@Parameter(name = "status", description = "Status filter parameter") @RequestParam(value = "status", required = false) final List<Status> status,
		@Parameter(name = "category", description = "Category filter parameter") @RequestParam(value = "category", required = false) final List<Category> category) {

		return ok(disturbanceService.findByMunicipalityIdAndPartyIdAndCategoryAndStatus(municipalityId, partyId, category, status));
	}

	@PatchMapping(path = "/{category}/{disturbanceId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Manage updates of a disturbance. Should be used when the set of affected persons/organizations is changed or the disturbance description is updated.", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "409", description = "Conflict", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Disturbance> updateDisturbance(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281", required = true) @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "category", description = "Disturbance category", required = true) @PathVariable(name = "category") final Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @PathVariable(name = "disturbanceId") final String disturbanceId,
		@RequestBody @Valid final DisturbanceUpdateRequest body) {

		return ok(disturbanceService.updateDisturbance(municipalityId, category, disturbanceId, body));
	}

	@DeleteMapping(path = "/{category}/{disturbanceId}")
	@Operation(summary = "Delete a disturbance. Should be used when the disturbance is resolved. Any affected persons/organizations (with notification subscriptions) will be notified of the resolved disturbance.", responses = {
		@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
			Problem.class, ConstraintViolationProblem.class
		}))),
		@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
		@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> deleteDisturbance(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281", required = true) @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "category", description = "Disturbance category", required = true) @PathVariable(name = "category") final Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @PathVariable(name = "disturbanceId") final String disturbanceId) {

		disturbanceService.deleteDisturbance(municipalityId, category, disturbanceId);
		return noContent().build();
	}
}
