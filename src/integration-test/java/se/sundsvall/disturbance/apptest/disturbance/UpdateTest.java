package se.sundsvall.disturbance.apptest.disturbance;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;
import se.sundsvall.disturbance.api.model.Category;

/**
 * Update disturbance application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/disturbance/UpdateTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class UpdateTest extends AbstractAppTest {

	private static final String PATH = "/2281/disturbances";

	@Test
	void test1_updateDisturbanceContent() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-5";

		setupCall()
			.withServicePath(PATH + "/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_updateDisturbanceRemoveAffecteds() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-6";

		setupCall()
			.withServicePath(PATH + "/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_updateDisturbanceAddAffecteds() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-7";

		setupCall()
			.withServicePath(PATH + "/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_updateDisturbanceChangeStatusToClosed() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-8";

		setupCall()
			.withServicePath(PATH + "/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_updateDisturbanceChangeStatusFromPlannedToOpen() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-12";

		setupCall()
			.withServicePath(PATH + "/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_updateDisturbanceContentChangedAndNewAffectedAdded() {

		final var category = Category.ELECTRICITY;
		final var disturbanceId = "disturbance-13";

		setupCall()
			.withServicePath(PATH + "/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_updateDisturbanceChangeAffectedProperties() {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbance-2";

		setupCall()
			.withServicePath(PATH + "/" + category + "/" + disturbanceId)
			.withHttpMethod(PATCH)
			.withRequest("request.json")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
