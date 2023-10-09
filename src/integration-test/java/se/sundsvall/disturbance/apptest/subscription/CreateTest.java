package se.sundsvall.disturbance.apptest.subscription;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;

/**
 * Create subscription application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/subscription/CreateTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class CreateTest extends AbstractAppTest {

	@Test
	void test1_createSubscription() throws IOException {

		final var headers = setupCall()
			.withServicePath("/subscriptions")
			.withHttpMethod(POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.withExpectedResponseHeader(LOCATION, List.of("http://localhost:(.*)/subscriptions/(\\d+)"))
			.sendRequest()
			.getResponseHeaders();

		setupCall()
			.withServicePath(headers.get(LOCATION).stream().findFirst().get())
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
