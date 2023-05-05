package se.sundsvall.disturbance.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;

/**
 * Create disturbance feedback application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/CreateDisturbanceFeedbackTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata.sql"
})
class CreateDisturbanceFeedbackTest extends AbstractAppTest {

	@Autowired
	private DisturbanceFeedbackRepository disturbanceFeedbackRepository;

	@Test
	void test1_createDisturbanceFeedback() throws Exception {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-11";
		final var partyId = "3807839a-3bab-11ec-8d3d-0242ac130003";

		assertThat(disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyId(category, disturbanceId, partyId)).isNotPresent();

		setupCall()
			.withServicePath("/disturbances/" + category + "/" + disturbanceId + "/feedback")
			.withHttpMethod(POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(disturbanceFeedbackRepository.findByCategoryAndDisturbanceIdAndPartyId(category, disturbanceId, partyId)).isPresent();
	}
}
