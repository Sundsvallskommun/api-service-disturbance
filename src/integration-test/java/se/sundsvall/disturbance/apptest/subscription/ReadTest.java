package se.sundsvall.disturbance.apptest.subscription;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;

/**
 * Read subscription application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/subscription/ReadTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class ReadTest extends AbstractAppTest {

	private static final String PATH = "/2281/subscriptions";
	private static final String ID = "1";
	private static final String PARTY_ID = "44f40c52-f550-4fee-860d-eda9c591d6a3";

	@Test
	void test1_readSubscriptionById() {

		setupCall()
			.withServicePath(PATH + "/" + ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_readSubscriptionByPartyId() {

		setupCall()
			.withServicePath(PATH + "?partyId=" + PARTY_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
