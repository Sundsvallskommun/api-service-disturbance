package se.sundsvall.disturbance.service;

final class ServiceConstants {

	private ServiceConstants() {}

	// Disturbance
	static final String ERROR_DISTURBANCE_NOT_FOUND = "No disturbance found for category:'%s' and id:'%s'!";
	static final String ERROR_DISTURBANCE_ALREADY_EXISTS = "A disturbance with category:'%s' and id:'%s' already exists!";
	static final String ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED = "The disturbance with category:'%s' and id:'%s' is closed! No updates are allowed on closed disturbances!";
	static final String ERROR_DISTURBANCE_CLOSED = "A disturbance with category:'%s' and id:'%s' exists, but is closed!";

	// Subscription
	static final String ERROR_SUBSCRIPTION_ALREADY_EXISTS = "A subscription entity for partyId:'%s' already exists!";
	static final String ERROR_SUBSCRIPTION_NOT_FOUND_BY_ID = "No subscription entity found for id:'%s'!";
	static final String ERROR_SUBSCRIPTION_NOT_FOUND_BY_PARTY_ID = "No subscription entity found for partyId:'%s'!";
}
