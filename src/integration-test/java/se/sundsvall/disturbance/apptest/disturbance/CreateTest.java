package se.sundsvall.disturbance.apptest.disturbance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withCategory;
import static se.sundsvall.disturbance.integration.db.specification.DisturbanceSpecification.withDisturbanceId;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;

/**
 * Create disturbance application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/disturbance/CreateTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class CreateTest extends AbstractAppTest {

	@Autowired
	private DisturbanceRepository disturbanceRepository;

	@Test
	void test1_createDisturbance() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-1";

		assertThat(disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)))).isNotPresent();

		setupCall()
			.withServicePath("/disturbances")
			.withHttpMethod(POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.withExpectedResponseHeader(LOCATION, List.of("/disturbances/" + category + "/" + disturbanceId))
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)))).isPresent();
	}

	@Test
	void test2_createDisturbanceWhenSubscriptionExists() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-with-subscription-1";

		assertThat(disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)))).isNotPresent();

		setupCall()
			.withServicePath("/disturbances")
			.withHttpMethod(POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.withExpectedResponseHeader(LOCATION, List.of("/disturbances/" + category + "/" + disturbanceId))
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceRepository.findOne(withCategory(category).and(withDisturbanceId(disturbanceId)))).isPresent();
	}
}
