package se.sundsvall.disturbance.apptest.disturbance;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;

/**
 * Delete disturbance application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/disturbance/DeleteTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class DeleteTest extends AbstractAppTest {

	private static final String PATH = "/2281/disturbances/";

	@Test
	void test1_deleteDisturbanceWithStatusOpen() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-9";

		setupCall()
			.withServicePath(PATH + category + "/" + disturbanceId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_deleteDisturbanceWithStatusClosed() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-10";

		setupCall()
			.withServicePath(PATH + category + "/" + disturbanceId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}
}
