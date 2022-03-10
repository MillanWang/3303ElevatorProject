package tests.SchedulerTests;

import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import app.Scheduler.ElevatorSpecificFloorsToVisit;

public class ElevatorSpecificFloorsToVisitTest {
	private ElevatorSpecificFloorsToVisit esftv;

	/**
	 * Ensure that the getters return the values defined in the constructor
	 */
	@Test
	public void testElevatorSpecificFloorsToVisit_gettersGetConstructorValues() {
		TreeSet<Integer> ts = new TreeSet<Integer>();
		this.esftv = new ElevatorSpecificFloorsToVisit(ts, 1);
		Assert.assertTrue(esftv.getFloorsToVisit().equals(ts));
		Assert.assertTrue(esftv.getElevatorID() == 1);
	}
}
