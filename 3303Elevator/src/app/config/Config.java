package app.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class Config {

	private Properties prop;
	
	public Config(String filename){
		this.prop = new Properties();
		try {
			String path = "src/app/config/" + filename;
			File file = new File(path);
			InputStream is = new FileInputStream(file);
			prop.load(is);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public String get(String key) {
		return prop.getProperty(key);
	}
	
	public static void main(String[] args) {
		Config config = new Config("local.config");
		
		System.out.print(config.get("scheduler.port"));
	}
	
}
