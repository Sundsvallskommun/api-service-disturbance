package se.sundsvall.disturbance.apptest.disturbance;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.disturbance.Application;

/**
 * Read disturbance application tests
 *
 * @see src/test/resources/db/scripts/testdata.sql for data setup.
 */
@WireMockAppTestSuite(files = "classpath:/disturbance/ReadTest/", classes = Application.class)
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class ReadTest extends AbstractAppTest {

	private static final String PATH = "/2281/disturbances";

	@Test
	void test1_readDisturbanceById() {

		final var disturbanceId = "disturbance-2";

		setupCall()
			.withServicePath(PATH + "/COMMUNICATION/" + disturbanceId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_readDisturbanceByPartyId() {

		final var partyId = "c76ae496-3aed-11ec-8d3d-0242ac130003";

		setupCall()
			.withServicePath(PATH + "/affecteds/" + partyId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_readDisturbanceByPartyIdAndCategoryFilter() {

		final var partyId = "c76ae496-3aed-11ec-8d3d-0242ac130003";

		setupCall()
			.withServicePath(PATH + "/affecteds/" + partyId + "?category=COMMUNICATION")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_readDisturbanceByPartyIdAndStatusFilter() {

		final var partyId = "c76ae496-3aed-11ec-8d3d-0242ac130003";

		setupCall()
			.withServicePath(PATH + "/affecteds/" + partyId + "?status=OPEN")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_readDisturbanceByPartyIdWithNoResults() {

		final var partyId = "887a45da-dbd4-4d58-98bc-4afe3c2ecc18"; // Doesn't exist in DB.

		setupCall()
			.withServicePath(PATH + "/affecteds/" + partyId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_readDisturbancesWithStatusAndCategoryFilter() {
		setupCall()
			.withServicePath(PATH + "?status=OPEN&category=COMMUNICATION")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_readDisturbancesWithNoFilter() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}
}
