package app.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/***
 * Class used to load configuration files
 * @author Ben Kittilsen
 *
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
	 * Used to get a properties from a configuration file that was loaded
	 * 
	 * @param key the key or properties that a user is looking 
	 * for within the configuration file loaded
	 * @return the value for that key
	 */
	public String get(String key) {
		return prop.getProperty(key);
	}
		
}
