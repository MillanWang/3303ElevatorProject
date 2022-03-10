package app.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/***
 * Class used to load configuration files
 * @author Ben Kittilsen
 */
public class Config {
	
	/***
	 * Properties from the loaded configuration file
	 */
	private Properties prop;
	
	/***
	 * Loads Properties from a configuration file looks in default 
	 * configuration path. 
	 * Default path is "src/app/config"
	 * 
	 * @param filename for the file in the default directory
	 */
	public Config(String filename){
		this("src/app/config", filename);
	}
	
	/***
	 * Load Properties from a configuration file given path and filename
	 * 
	 * @param path location configuration file is stored
	 * @param filename configuration file name
	 */
	public Config(String path, String filename){
		this.prop = new Properties();
		try {
			File file = new File(path + '/' + filename);
			InputStream is = new FileInputStream(file);
			prop.load(is);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/***
	 * gets a string property for a key otherwise throws an error that it cant be found
	 * @param key
	 * @return
	 */
	private String getProperty(String key) {
		String value = prop.getProperty(key, null);
		if(value==null) {
			System.out.print("[ERROR]Config#getProperty(String key) failed to load config value for "+ key);
			System.exit(1);
		}
		return value;
	}
	
	/***
	 * Used to get a properties from a configuration file that was loaded
	 * 
	 * @param key the key or properties that a user is looking 
	 * for within the configuration file loaded
	 * @return the value for that key
	 */
	public String getString(String key) {
		return this.getProperty(key);
	}
	
	/***
	 * Used to get a variable expected to be an integer. if variable is not an integer will exit program
	 * 
	 * @param key that the value is stored under
	 * @return the integer value for that key
	 */
	public int getInt(String key) {
		String value = this.getProperty(key);
		int res = Integer.MIN_VALUE;
		try {
			res = Integer.parseInt(value);
		}catch(Exception e) {
			System.out.print("[ERROR]Config#getInt(String key) failed to convert string to int for configuration property "+ key);
			System.exit(1);
		}
		return res;
	}
	
	/***
	 * Used to retrieve the boolean value for a property key
	 * 
	 * @param key that the variable is stored for
	 * @return the boolean value for that variable
	 */
	public boolean getBoolean(String key) {
		String value = this.getProperty(key);
		boolean res = false;
		try {
			res = Boolean.parseBoolean(value);
		}catch(Exception e) {
			System.out.print("[ERROR]Config#getBoolean(String key) failed to convert string to boolean for configuration property "+ key);
			System.exit(1);
		}
		return res;
	}
		
}
