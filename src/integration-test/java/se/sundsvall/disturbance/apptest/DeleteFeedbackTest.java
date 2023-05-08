package se.sundsvall.disturbance.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;

/**
 * Delete feedback application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/DeleteFeedbackTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata.sql"
})
class DeleteFeedbackTest extends AbstractAppTest {

	@Autowired
	private FeedbackRepository feedbackRepository;

	@Test
	void test1_deleteFeedback() throws Exception {

		final var partyId = "3c1236ca-4c44-11ec-81d3-0242ac130003";

		assertThat(feedbackRepository.findByPartyId(partyId)).isPresent();

		setupCall()
			.withServicePath("/feedback/" + partyId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(feedbackRepository.findByPartyId(partyId)).isNotPresent();
	}
}
