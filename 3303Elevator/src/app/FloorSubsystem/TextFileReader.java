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
	public static ArrayList<Input> getrequests(String inputfile){
		ArrayList<Input> requests = new ArrayList<Input>(); //array list of all requests from text file
		String line; //line being parsed in the text file
		boolean upward; //boolean representing if the elevator is to go upward from the current floor (True if Up, False if Down) 
		try {
			BufferedReader reader  = new BufferedReader(new FileReader(inputfile));
			line = reader.readLine();
			while (line != null) {
				String[] values = line.split(",");
//				try {
//					System.out.println(LocalTime.parse(values[0]));
//				}
//				catch(Exception e){
//					System.out.println("time is null");
//				}
//				System.out.println(Integer.valueOf(values[1]));
//				System.out.println(String.valueOf(values[2]));
//				System.out.println(Integer.valueOf(values[3]));
				if(String.valueOf(values[2]).equals("Up")){
					upward = true;
				}
				else {
					upward =  false; 
				}
				LocalTime time;
				try{
					time = LocalTime.parse(values[0]);
				}
				catch(Exception e) {
					time = null;
				}
				Integer source = Integer.valueOf(values[1]);
				Integer destination = Integer.valueOf(values[3]);
				Input event = new Input(time , source , upward , destination); 
				requests.add(event);
				line = reader.readLine();
			}
			reader.close(); 
		}catch (IOException e) {
			e.printStackTrace();
		}
		return requests;
		
	}	
}
