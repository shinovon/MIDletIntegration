package midletintegration;

import java.util.Hashtable;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.midlet.MIDlet;

/**
 * MIDlet Integration library<br>
 * Works only on Symbian^3 and above
 * 
 * @author Shinovon
 * @version 0.1
 * 
 */
public class MIDletIntegration {
	
	private static final String PROTOCOL = "localapp://jam/launch?";
	
	private static int instances;

	/**
	 * Checks if a MIDlet has received a new start request from another MIDlet<br>
	 * Recommended to use in startApp() with "Nokia-MIDlet-Background-Event: pause" property in MANIFEST.MF<br>
	 * After receiving a request, you should receive arguments from System.getProperty() or checkStartArguments()
	 * @see {@link #getAllLaunchArguments()}
	 * @see {@link java.lang.System#getProperty(String)}
	 * @return true if new arguments have been received since the last check
	 */
	public static boolean checkLaunchArguments() {
		if(System.getProperty("com.nokia.mid.cmdline.instance") == null)
			return false;
		try {
			int i = Integer.parseInt(System.getProperty("com.nokia.mid.cmdline.instance")) ;
			if(i > instances) {
				instances = i;
				String cmd = System.getProperty("com.nokia.mid.cmdline");
				if(cmd == null || cmd.length() == 0) {
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Gets all received launch arguments from <code>com.nokia.mid.cmdline</code> property<br>
	 * and converts it to {@link java.util.Hashtable}
	 * @see {@link #checkLaunchArguments()}
	 * @see {@link midletintegration.Util#parseArgs(String)}
	 * @see {@link java.lang.System#getProperty(String)}
	 * @return Arguments table or null
	 */
	public static Hashtable getAllLaunchArguments() {
		return Util.parseArgs(System.getProperty("com.nokia.mid.cmdline"));
	}
	
	/**
	 * Runs a MIDlet by Name/Vendor
	 * @see {@link #startApp(MIDlet, String, String, String)}
	 * @see {@link javax.microedition.midlet.MIDlet#platformRequest(String)}
	 * @param midlet Current MIDlet instance
	 * @param name MIDlet-Name
	 * @param vendor MIDlet-Vendor
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 * @throws ConnectionNotFoundException
	 */
	public static boolean startApp(MIDlet midlet, String name, String vendor) throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException {
		return startApp(midlet, name, vendor, null);
	}
	
	/**
	 * Runs a MIDlet by Name/Vendor with arguments
	 * @see {@link javax.microedition.midlet.MIDlet#platformRequest(String)}
	 * @param midlet Current MIDlet instance
	 * @param name MIDlet-Name
	 * @param vendor MIDlet-Vendor
	 * @param args Start arguments
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 * @throws ConnectionNotFoundException
	 */
	public static boolean startApp(MIDlet midlet, String name, String vendor, String args) throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException {
		return startApp(midlet, "midlet-name=" + Util.encodeURL(name) + ";midlet-vendor=" + Util.encodeURL(vendor) + (args != null && args.length() > 0 ? ";" + Util.encodeURL(args) : ""));
	}

	
	/**
	 * Starts a MIDlet by UID
	 * @see {@link #startAppWithAppUID(MIDlet, String, String)}
	 * @see {@link javax.microedition.midlet.MIDlet#platformRequest(String)}
	 * @param midlet Current MIDlet instance
	 * @param uid App's "Nokia-MIDlet-UID-1" value
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 * @throws ConnectionNotFoundException
	 */
	public static boolean startAppWithAppUID(MIDlet midlet, String uid) throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException {
		return startAppWithAppUID(midlet, uid, null);
	}
	
	/**
	 * Runs a MIDlet by UID with arguments
	 * @see {@link #startAppWithAppUID(MIDlet, String, String)}
	 * @see {@link javax.microedition.midlet.MIDlet#platformRequest(String)}
	 * @param midlet Current MIDlet instance
	 * @param uid App's "Nokia-MIDlet-UID-1" value
	 * @param args Start arguments
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 * @throws ConnectionNotFoundException
	 */
	public static boolean startAppWithAppUID(MIDlet midlet, String uid, String args) throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException {
		return startApp(midlet, "midlet-uid=" + Util.encodeURL(uid) + (args != null && args.length() > 0 ? ";" + Util.encodeURL(args) : ""));
	}
	
	private static boolean startApp(MIDlet midlet, String args) throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException {
		try {
			return midlet.platformRequest(PROTOCOL + args);
		} catch (ConnectionNotFoundException e) {
			if(e.getMessage() != null) {
				if(e.getMessage().startsWith("Cannot start Java application") ||
						e.getMessage().indexOf("following error: -12") != -1) {
					throw new MIDletNotFoundException(e.getMessage());
				} else if(e.getMessage().indexOf("Invalid localapp URL") != -1 ||
						e.getMessage().indexOf("Invalid URL") != -1) {
					throw new ProtocolNotSupportedException(e.getMessage());
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
	}
	
}
