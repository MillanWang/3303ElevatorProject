/**
 * Elevator project 
 * This class tests the TextFileReader that reads the input file that has the elevator's requests 
 * @author Peter Tanyous 
 * #ID 101127203 
 */

package tests.FloorSubsystemTests;


import static org.junit.Assert.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import app.FloorSubsystem.ScheduledElevatorRequest;
import app.FloorSubsystem.TextFileReader;

public class TextFileReaderTest {
	ArrayList<ScheduledElevatorRequest> reqs;
	
	@Test
	/**
	 * getRequestTest reads an empty file to ensure nothing gets added and tests an input file with 9 requests to ensure all are added accordingly 
	 */
	public void getRequestTest() {
		reqs = TextFileReader.getrequests("src/app/FloorSubsystem/emptyinputfile.txt");
		assertEquals(0, reqs.size());
		reqs = TextFileReader.getrequests("src/app/FloorSubsystem/inputfile.txt");
		assertEquals(9,reqs.size());
		assertNull(reqs.get(reqs.size() - 1).getTime());
		assertNotNull(reqs.get(0).getTime());
	}
}
