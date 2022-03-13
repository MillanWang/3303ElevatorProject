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
	public void testElevatorSpecificFloorsToVisit_IdSetProperly(){
		this.esftv = new ElevatorSpecificFloorsToVisit(1);
		Assert.assertTrue(esftv.getElevatorID() == 1);
	}
	
	
	@Test
	public void testElevatorSpecificFloorsToVisit_AddUpwards(){
		this.esftv = new ElevatorSpecificFloorsToVisit(1);
		esftv.addUpwardsFloorToVisit(43);
		esftv.addUpwardsFloorToVisit(43);
		esftv.addUpwardsFloorToVisit(12);
		
		Assert.assertTrue(esftv.getUpwardsFloorsToVisit().size()==2);
		Assert.assertTrue(esftv.getActiveRequestCount()==2);
	}
	
	@Test
	public void testElevatorSpecificFloorsToVisit_AddDownwards(){
		this.esftv = new ElevatorSpecificFloorsToVisit(1);
		esftv.addDownwardsFloorToVisit(43);
		esftv.addDownwardsFloorToVisit(43);
		esftv.addDownwardsFloorToVisit(12);
		
		Assert.assertTrue(esftv.getDownwardsFloorsToVisit().size()==2);
		Assert.assertTrue(esftv.getActiveRequestCount()==2);
	}
	
	@Test
	public void testElevatorSpecificFloorsToVisit_AddAndRemove(){
		this.esftv = new ElevatorSpecificFloorsToVisit(1);
		esftv.addDownwardsFloorToVisit(43);
		esftv.addUpwardsFloorToVisit(43);
		esftv.addDownwardsFloorToVisit(12);
		
		Assert.assertTrue(esftv.getDownwardsFloorsToVisit().size()==2);
		Assert.assertTrue(esftv.getUpwardsFloorsToVisit().size()==1);
		Assert.assertTrue(esftv.getActiveRequestCount()==3);
		
		esftv.downwardsFloorIsVisited(12);
		esftv.upwardsFloorIsVisited(43);
		
		Assert.assertTrue(esftv.getDownwardsFloorsToVisit().size()==1);
		Assert.assertTrue(esftv.getUpwardsFloorsToVisit().size()==0);
		Assert.assertTrue(esftv.getActiveRequestCount()==1);
		
	}
}
