/*
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
	
	
	public static ArrayList<Input> getrequests(String inputfile){
		ArrayList<Input> requests = new ArrayList<Input>(); //array list of all requests from text file
		String line; //line being parsed in the text file
		boolean upward; //boolean representing if the elevator is to go upward from the current floor (True if Up, False if Down) 
		try {
			BufferedReader reader  = new BufferedReader(new FileReader(inputfile));
			line = reader.readLine();
			while (line != null) {
				String[] values = line.split(",");
				//System.out.println(LocalTime.parse(values[0]));
				//System.out.println(Integer.valueOf(values[1]));
				//System.out.println(String.valueOf(values[2]));
				//System.out.println(Integer.valueOf(values[3]));
				if(String.valueOf(values[2]).equals("Up")){
					upward = true;
				}
				else {
					upward =  false; 
				}
				Input event = new Input(LocalTime.parse(values[0]) , Integer.valueOf(values[1]) , upward , Integer.valueOf(values[3])); 
				requests.add(event);
				line = reader.readLine();
			}
			reader.close(); 
		}catch (IOException e) {
			e.printStackTrace();
		}
		return requests;
		
	}

//	public static void main(String[] args) {
//		getrequests("C:/Users/peter/Desktop/Winter_2022/SYSC_3303_Assignments/3303ElevatorProject/3303ElevatorProject/3303Elevator/src/app/FloorSubsystem/inputfile.txt");
//	}
	
}
