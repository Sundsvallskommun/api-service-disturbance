package se.sundsvall.disturbance.apptest.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.integration.db.SubscriptionRepository;

/**
 * Delete subscription application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/subscription/DeleteTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class DeleteTest extends AbstractAppTest {

	private static final Long ID = 1L;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Test
	void test1_deleteSubscriptionById() {

		assertThat(subscriptionRepository.findById(ID)).isPresent();

		setupCall()
			.withServicePath("/subscriptions/" + ID)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(subscriptionRepository.findById(ID)).isNotPresent();

	}
}
