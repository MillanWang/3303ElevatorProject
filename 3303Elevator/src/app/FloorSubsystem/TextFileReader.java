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
	public static ArrayList<ScheduledElevatorRequest> getrequests(String inputfile){
		ArrayList<ScheduledElevatorRequest> requests = new ArrayList<ScheduledElevatorRequest>(); //array list of all requests from text file
		String line; //line being parsed in the text file
		boolean upward; //boolean representing if the elevator is to go upward from the current floor (True if Up, False if Down) 
		LocalTime time; //time at which the call is made: read from the input file 
		Integer source; //source floor where the elevator was called 
		Integer destination; // destination floor where the elevator if requested to go 
		try {
			BufferedReader reader  = new BufferedReader(new FileReader(inputfile));
			line = reader.readLine();
			while (line != null) {

				upward = getIsUpward(line);
				time = getTime(line);
				source = getSource(line);
				destination = getDestination(line);
				ScheduledElevatorRequest event = new ScheduledElevatorRequest(time , source , upward , destination); 
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
	 * gets the direction of the elevator request 
	 * 
	 * @param line: the line read from the txt file 
	 * @return true if the direction is upward, false if downward
	 */
	public static boolean getIsUpward(String line) {
		String[] lineValues = line.split(",");
		if(String.valueOf(lineValues[2]).equals("Up")){
			return  true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * gets the time the request was made from the txt file 
	 * 
	 * @param line: the line read from the txt file 
	 * @return the time the request was made as logged in the input file.
	 */
	public static LocalTime getTime(String line) {
		String[] lineValues = line.split(",");
		try{
			return LocalTime.parse(lineValues[0]);
		}
		catch(Exception e) {
			
			return null; 
		}
	}
	
	/**
	 * gets the source where the request was made from in the txt file
	 * 
	 * @param line: the line read from the txt file 
	 * @return int: the floor number of the source 
	 */
	public static int getSource(String line) {
		String[] lineValues = line.split(",");
		
		return Integer.valueOf(lineValues[1]);
		
	}
	
	/**
	 * gets the destination the elevator is to go to in the txt file
	 * 
	 * @param line: the line read from the txt file 
	 * @return int: the floor number of the destination  
	 */
	public static int getDestination(String line) {
		String[] lineValues = line.split(",");
		return Integer.valueOf(lineValues[3]);
	}
	
	
}
