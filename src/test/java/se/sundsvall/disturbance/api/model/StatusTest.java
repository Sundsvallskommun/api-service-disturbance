package se.sundsvall.disturbance.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.disturbance.api.model.Status.CLOSED;
import static se.sundsvall.disturbance.api.model.Status.OPEN;
import static se.sundsvall.disturbance.api.model.Status.PLANNED;

class StatusTest {

	@Test
	void statusEnum() {
		assertThat(Status.values()).containsExactly(OPEN, CLOSED, PLANNED);
	}
}
