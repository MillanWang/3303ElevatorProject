/**
 * Elevator project 
 * This class reads the text input file that has the elevator's requests 
 * @author Peter Tanyous 
 * #ID 101127203 
 */

package app.FloorSubsystem;
import java.io.*;
import java.time.LocalTime;
import java.util.*;

public class TextFileReader {
	
	/**
	 * Reads a file and creates an arrayList of Input objects to be passed to the Scheduler
	 * @param inputfile file to be read
	 * @return ArrayList of input files
	 */
	public static ArrayList<ScheduledElevatorRequest> getRequests(String inputFilePath){
		ArrayList<ScheduledElevatorRequest> requests = new ArrayList<ScheduledElevatorRequest>(); //array list of all requests from text file
		String line; //line being parsed in the text file
		
		try {
			BufferedReader reader  = new BufferedReader(new FileReader(inputFilePath));
			line = reader.readLine();
			while (line != null) {

				ScheduledElevatorRequest event = getRequest(line); 
				requests.add(event);
				line = reader.readLine();
			}
			reader.close(); 
		}catch (IOException e) {
			e.printStackTrace();
		}
		return requests;
		
	}
	
	/**
	 * parses a request line and returns a ScheduledElevatorRequest object
	 * @param line: the line read from the file
	 * @return ScheduledElevatorRequest Object
	 */
	public static ScheduledElevatorRequest getRequest(String line) {
		boolean isUpward;
		LocalTime time;
		Integer source;
		Integer destination;
		
		String[] lineValues = line.split(",");
		
		if(String.valueOf(lineValues[2]).equals("Up")){
			isUpward =  true;
		}
		else {
			isUpward = false;
		}
		try{
			time =  LocalTime.parse(lineValues[0]);
		}
		catch(Exception e) {
			
			time = null;
		}
		source = Integer.valueOf(lineValues[1]);
		destination = Integer.valueOf(lineValues[3]);
		ScheduledElevatorRequest event = new ScheduledElevatorRequest(time , source , isUpward , destination);
		return event; 
		
	}
	
	
}
