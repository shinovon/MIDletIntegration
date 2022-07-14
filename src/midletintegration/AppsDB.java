package midletintegration;

/**
 * Part of MIDlet Integration library
 * 
 * @author Shinovon
 * @version 0.1
 * 
 */
public class AppsDB {

	public static final App ExampleReceiver = new App(0, "Example Receiver", "Example", "0xA0001234", 1270);
	
	public static class App {
		String name;
		String vendor;
		String uid;
		int port;
		int id;

		private App(int id, String name, String vendor) {
			this.id = id;
			this.name = name;
			this.vendor = vendor;
		}

		private App(int id, String name, String vendor, String uid) {
			this.id = id;
			this.name = name;
			this.vendor = vendor;
			this.uid = uid;
		}
		
		private App(int id, String name, String vendor, String uid, int port) {
			this.id = id;
			this.name = name;
			this.vendor = vendor;
			this.uid = uid;
			this.port = port;
		}
	}
}
