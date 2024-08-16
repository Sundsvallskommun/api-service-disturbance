package se.sundsvall.disturbance.apptest.disturbance;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;

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

	private static final String PATH = "/2281/disturbances";

	@Test
	void test1_createDisturbance() {

		final var headers = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders();

		setupCall()
			.withServicePath(headers.get(LOCATION).getFirst())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_createDisturbanceWhenSubscriptionExists() {

		final var headers = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders();

		setupCall()
			.withServicePath(headers.get(LOCATION).getFirst())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
