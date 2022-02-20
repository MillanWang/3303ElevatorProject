/**
 * 
 */
package tests.SchedulerTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.Scheduler.TimeManagementSystem;
import java.util.ArrayList;
/**
 * SYSC 3303, Final Project Iteration 1&0
 * TimeManagementTest.java
 * 
 * Time management tester class
 * 
 * @author Abdelrahim Karaja
 * 
 */
public class TimeManagementTest {
	TimeManagementSystem tms;
	
	/**
	 * Tests that a multiplier of 0 will only return 0 values from the 2 time management functions
	 *
	 */
	@Test
	public void testZero() {
		//Testing that a 0 multiplier creates only 0s outputs
		tms = new TimeManagementSystem(0);
		float t = tms.getElevatorLoadingTime();
		assertEquals("Returns 0 properly", 0f, t, 0f);
		
		ArrayList<Float> times = tms.getElevatorTransitTime(1, 5);
		for(int i = 0; i < times.size(); i++) {
			assertEquals("Array of times is 0", 0f, times.get(i), 0f);
		}
	}
	
	/**
	 * Tests that a multiplier of 1 will only return values within base range from the 2 time management functions
	 *
	 */
	@Test
	public void testOne(){
		//Testing that a 1 multiplier creates only outputs within normal range
		tms = new TimeManagementSystem(1);
		float t = tms.getElevatorLoadingTime();
		assertTrue("Returns within normal range", 7920.0f <= t && t <= 11140.0f);

		ArrayList<Float> times = tms.getElevatorTransitTime(1, 5);
		//Output of movement at max velocity
		for(int i = 1; i < times.size() - 1; i++) {
			assertTrue("Array of times is within normal range at max velocity", 1900f <= times.get(i) && times.get(i) <= 2100f);
		}
		
		//Movement at initial floor and last floor (acceleration and deceleration)
		assertTrue("Times is within normal range from rest to max", 3800f <= times.get(0) && times.get(0) <= 4200f);
		assertTrue("Times is within normal range from max to rest", 3800f <= times.get(times.size()-1) && times.get(times.size()-1) <= 4200f);
		
		//Testing movement output for only 1 floor travel
		times = tms.getElevatorTransitTime(1, 2);
		assertTrue("Times is within normal range for 1 floor travelled", 1900f <= times.get(0) && times.get(0) <= 2100f);
	}
	
	/**
	 * Tests that a multiplier of 5 will only return values from within range including the multiplier the 2 time management functions
	 *
	 */
	@Test
	public void testFive(){
		//Testing that a 5 multiplier creates proper outputs
		tms = new TimeManagementSystem(5);
		float t = tms.getElevatorLoadingTime();
		assertTrue("Returns within range scaled up 5", 5*7920f <= t && t <= 5*11140f);

		ArrayList<Float> times = tms.getElevatorTransitTime(1, 5);
		//Output of movement at max velocity
		for(int i = 1; i < times.size() - 1; i++) {
			assertTrue("Array of times is within normal range at max velocity", 5*1900f <= times.get(i) && times.get(i) <= 5*2100f);
		}
		
		//Movement at initial floor and last floor (acceleration and deceleration)
		assertTrue("Times is within normal range from rest to max", 5*3800f <= times.get(0) && times.get(0) <= 5*4200f);
		assertTrue("Times is within normal range from max to rest", 5*3800f <= times.get(times.size()-1) && times.get(times.size()-1) <= 5*4200f);
		
		//Testing movement output for only 1 floor travel
		times = tms.getElevatorTransitTime(1, 2);
		assertTrue("Times is within normal range for 1 floor travelled", 5*1900f <= times.get(0) && times.get(0) <= 5*2100f);
	}
	
	/**
	 * Tests that a negative multiplier of (-1) will only return POSITIVE values within base range from the 2 time management functions
	 *
	 */
	@Test
	public void testNegativeOne(){
		//Testing that a 1 multiplier creates only outputs within normal range
		tms = new TimeManagementSystem(-1);
		float t = tms.getElevatorLoadingTime();
		assertTrue("Returns within normal range", 7920.0f <= t && t <= 11140.0f);
		
		ArrayList<Float> times = tms.getElevatorTransitTime(1, 5);
		//Output of movement at max velocity
		for(int i = 1; i < times.size() - 1; i++) {
			assertTrue("Array of times is within normal range at max velocity", 1900f <= times.get(i) && times.get(i) <= 2100f);
		}
		
		//Movement at initial floor and last floor (acceleration and deceleration)
		assertTrue("Times is within normal range from rest to max", 3800f <= times.get(0) && times.get(0) <= 4200f);
		assertTrue("Times is within normal range from max to rest", 3800f <= times.get(times.size()-1) && times.get(times.size()-1) <= 4200f);
		
		//Testing movement output for only 1 floor travel
		times = tms.getElevatorTransitTime(1, 2);
		assertTrue("Times is within normal range for 1 floor travelled", 1900f <= times.get(0) && times.get(0) <= 2100f);
	}
	
	/**
	 * Tests that a fractional multiplier of (0.5) will only return values within base range including the multiplier from the 2 time management functions
	 *
	 */
	@Test
	public void testFrac(){
		//Testing that a 1 multiplier creates only outputs within normal range
		tms = new TimeManagementSystem(0.5f);
		float t = tms.getElevatorLoadingTime();
		assertTrue("Returns within normal range", 0.5 * 7920.0f <= t && t <= 0.5 * 11140.0f);
		
		ArrayList<Float> times = tms.getElevatorTransitTime(1, 5);
		//Output of movement at max velocity
		for(int i = 1; i < times.size() - 1; i++) {
			assertTrue("Array of times is within normal range at max velocity", 0.5*1900f <= times.get(i) && times.get(i) <= 0.5*2100f);
		}
		
		//Movement at initial floor and last floor (acceleration and deceleration)
		assertTrue("Times is within normal range from rest to max", 0.5*3800f <= times.get(0) && times.get(0) <= 0.5*4200f);
		assertTrue("Times is within normal range from max to rest", 0.5*3800f <= times.get(times.size()-1) && times.get(times.size()-1) <= 0.5*4200f);
		
		//Testing movement output for only 1 floor travel
		times = tms.getElevatorTransitTime(1, 2);
		assertTrue("Times is within normal range for 1 floor travelled", 0.5*1900f <= times.get(0) && times.get(0) <= 0.5*2100f);
		}
	}
