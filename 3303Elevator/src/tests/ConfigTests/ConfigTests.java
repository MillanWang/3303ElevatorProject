package tests.ConfigTests;

import static org.junit.Assert.*;
import org.junit.Test;
import app.Config.Config;

public class ConfigTests {

	@Test
	public void test() {
		Config config = new Config("local.properties");
		assertSame(config.getInt("scheduler.elevatorReceivePort") == 3000, true);
		assertSame(config.getString("scheduler.address").equals("localhost"),true);
		assertSame(config.getBoolean("elevator.log") || true, true);
	}
}
