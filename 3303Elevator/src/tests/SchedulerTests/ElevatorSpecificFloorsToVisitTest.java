package tests.SchedulerTests;

import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import app.Scheduler.ElevatorSpecificFloorsToVisit;

public class ElevatorSpecificFloorsToVisitTest {
	
	
	//TODO : Enhance testing to cover simulate elevator movement
	
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
		esftv.addRequest(2, 4);
		esftv.addRequest(3, 4);
		
		Assert.assertEquals(2, esftv.getUpwardsFloorsToVisit().size());
		Assert.assertEquals(3,esftv.getActiveNumberOfStopsCount());
	}
	
	@Test
	public void testElevatorSpecificFloorsToVisit_AddDownwards(){
		this.esftv = new ElevatorSpecificFloorsToVisit(1);
		esftv.addDownwardsFloorToVisit(43);
		esftv.addDownwardsFloorToVisit(43);
		esftv.addDownwardsFloorToVisit(12);
		
		Assert.assertTrue(esftv.getDownwardsFloorsToVisit().size()==2);
		Assert.assertTrue(esftv.getActiveNumberOfStopsCount()==2);
	}
	
	@Test
	public void testElevatorSpecificFloorsToVisit_AddAndRemove(){
		this.esftv = new ElevatorSpecificFloorsToVisit(1);
		esftv.addDownwardsFloorToVisit(21);
		esftv.addUpwardsFloorToVisit(21);
		esftv.addDownwardsFloorToVisit(12);
		
		Assert.assertTrue(esftv.getDownwardsFloorsToVisit().size()==2);
		Assert.assertTrue(esftv.getUpwardsFloorsToVisit().size()==1);
		Assert.assertTrue(esftv.getActiveNumberOfStopsCount()==3);
		
		esftv.downwardsFloorIsVisited(12);
		esftv.upwardsFloorIsVisited(21);
		
		Assert.assertTrue(esftv.getDownwardsFloorsToVisit().size()==1);
		Assert.assertTrue(esftv.getUpwardsFloorsToVisit().size()==0);
		Assert.assertTrue(esftv.getActiveNumberOfStopsCount()==1);
		
	}
}
