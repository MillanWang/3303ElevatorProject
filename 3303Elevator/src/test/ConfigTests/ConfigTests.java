package test.ConfigTests;

import static org.junit.Assert.*;
import org.junit.Test;
import app.Config.Config;

public class ConfigTests {

	@Test
	public void test() {
		Config config = new Config("local.properties");
		assertSame(config.getInt("scheduler.port"), 3000);		
	}
}
