package se.sundsvall.disturbance.apptest;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;
import static se.sundsvall.disturbance.api.model.Status.OPEN;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withCategory;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withDisturbanceId;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

/**
 * Update disturbance application tests
 * 
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/UpdateDisturbanceTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata.sql"
})
@ActiveProfiles("junit")
class UpdateDisturbanceTest extends AbstractAppTest {

	@Autowired
	private DisturbanceRepository disturbanceRepository;

	@Test
	void test1_updateDisturbanceContent() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-5";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)));
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getTitle()).isEqualTo("Planerat avbrott i eget n??t");
		assertThat(updatedDisturbance.get().getDescription()).isEqualTo("Vi fels??ker str??mavbrottet.");
		assertThat(updatedDisturbance.get().getPlannedStopDate()).isEqualTo(LocalDateTime.of(2022, 01, 04, 18, 00, 20, 0).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(3);
	}

	@Test
	void test2_updateDisturbanceRemoveAffecteds() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-6";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)));
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(1);
	}

	@Test
	void test3_updateDisturbanceAddAffecteds() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-7";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)));
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(4);
	}

	@Test
	void test4_updateDisturbanceChangeStatusToClosed() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-8";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)));
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getStatus()).isEqualTo(CLOSED.toString());
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(3);
	}

	@Test
	void test5_updateDisturbanceChangeStatusFromPlannedToOpen() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-12";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)));
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getStatus()).isEqualTo(OPEN.toString());
		assertThat(updatedDisturbance.get().getAffectedEntities()).hasSize(3);
	}

	@Test
	void test6_updateDisturbanceContentChangedAndNewAffectedAdded() throws Exception {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-13";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		final var updatedDisturbance = disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)));
		assertThat(updatedDisturbance).isPresent();
		assertThat(updatedDisturbance.get().getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void test7_updateDisturbanceChangeAffectedProperties() throws Exception {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-2";

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
