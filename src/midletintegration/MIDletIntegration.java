package midletintegration;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.io.PushRegistry;
import javax.microedition.midlet.MIDlet;

/**
 * MIDlet Integration library
 * 
 * @author Shinovon
 * @author curoviyxru (Mathew)
 * @version 1.3
 * 
 */
public class MIDletIntegration implements Runnable {
	
	private static final String JAVAAPP_PROTOCOL = "localapp://jam/launch?";
	private static final String S40_LOCALAPP_URL = "http://nnp.nnchan.ru/nns/localapp.php?";
	
	private static final boolean supportsJavaApp = System.getProperty("com.nokia.mid.cmdline.instance") != null || Util.platform.indexOf("java_build_version=2.") != -1;
	private static final boolean s60 = Util.isS60();
	private static final boolean s40 = Util.isS40();
	
	private static int instances;
	private static DatagramConnection dataConnection;
	private static boolean receiving;

	private static Exception exception;
	
	private int pushPort;
	private String cmd;
	private Object lock;
	
	private MIDletIntegration(int port, String cmd, Object lock) {
		this.pushPort = port;
		this.cmd = cmd;
		this.lock = lock;
	}
	
	/**
	 * Checks if a MIDlet has received a new start request from other MIDlet<br>
	 * It is recommended to use in startApp() with "Nokia-MIDlet-Background-Event: pause" property in application descriptor<br>
	 * <p>
	 * After handling a request, you may get launch command by {@link #getLaunchCommand()}
	 * </p>
	 * @return true if new arguments have been received since the last check
	 */
	public static boolean checkLaunch() {
		if(receiving) return false;
		try {
			if(PushRegistry.listConnections(true).length > 0) {
				return true;
			}
		} catch (Throwable e) {
		}
		if(s40) {
			return (instances++) == 0 && System.getProperty("launchcmd") != null;
		}
		if(!supportsJavaApp) {
			return false;
		}
		try {
			int i = Integer.parseInt(System.getProperty("com.nokia.mid.cmdline.instance"));
			if(i > instances) {
				instances = i;
				String cmd = System.getProperty("com.nokia.mid.cmdline");
				return cmd != null && cmd.length() > 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Get received command
	 * 
	 * @return Received command, may be empty string
	 * <br>null if launch was not detected
	 */
	public static String getLaunchCommand() {
		receiving = true;
		String cmd = null;
		String[] connections = null;
		try {
			connections = PushRegistry.listConnections(true);
		} catch (Throwable e) {
		}
		if(connections != null && connections.length > 0) {
			try {
				DatagramConnection conn = (DatagramConnection) Connector.open(connections[0]);
				Datagram data = conn.newDatagram(conn.getMaximumLength());
				conn.receive(data);
				cmd = data.readUTF();
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
			}
		} else {
			cmd = System.getProperty("com.nokia.mid.cmdline");
			if(cmd == null) {
				cmd = System.getProperty("launchcmd");
			}
		}
		if("empty=1".equals(cmd)) {
			cmd = "";
		}
		receiving = false;
		return cmd;
	}
	
	/**
	 * Get launch source
	 * 
	 * @see {@link #getLaunchCommand()}
	 * @return Launcher app, may be null
	 */
	public static String getLaunchSource() {
		String r = null;
		l: {
			if((r = System.getProperty("from")) != null)
				break l;
			if((r = System.getProperty("launchfrom")) != null)
				break l;
			int i;
			if((r = System.getProperty("com.nokia.mid.cmdline")) != null && (i = r.indexOf("launchfrom=")) != -1) {
				r = r.substring(i + 5, (i = r.indexOf(';', i)) != -1 ? i : r.length());
				break l;
			}
			return null;
		}
		return r;
	}
	
	/**
	 * Converts string to arguments table
	 * 
	 * @see {@link #getLaunchCommand()}
	 * @return Arguments table or null
	 */
	public static Hashtable getArguments(String s) {
		return Util.parseArgs(s);
	}
	
	/**
	 * Launches a MIDlet by Name/Vendor
	 * @see {@link #startApp(MIDlet, String, String, String)}
	 * @see {@link javax.microedition.midlet.MIDlet#platformRequest(String)}
	 * @param midlet Current MIDlet instance
	 * @param name MIDlet-Name
	 * @param vendor MIDlet-Vendor
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 */
	public static boolean startApp(MIDlet midlet, String name, String vendor)
			throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException
	{
		return _launchApp(midlet, name, vendor, null, null);
	}
	
	/**
	 * Launches a MIDlet by Name/Vendor with arguments
	 * @see {@link javax.microedition.midlet.MIDlet#platformRequest(String)}
	 * @param midlet Current MIDlet instance
	 * @param name MIDlet-Name
	 * @param vendor MIDlet-Vendor
	 * @param cmd Command
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 */
	public static boolean startApp(MIDlet midlet, String name, String vendor, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException
	{
		return _launchApp(midlet, name, vendor, null, cmd);
	}
	
	/**
	 * Launches a MIDlet UID or Name/Vendor with arguments
	 * @see {@link javax.microedition.midlet.MIDlet#platformRequest(String)}
	 * @param midlet Current MIDlet instance
	 * @param name MIDlet-Name
	 * @param vendor MIDlet-Vendor
	 * @param cmd Command
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 */
	public static boolean startApp(MIDlet midlet, String name, String vendor, String uid, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException
	{
		return _launchApp(midlet, name, vendor, uid, cmd);
	}
	
	/**
	 * Runs a MIDlet by Name/Vendor or Push port with arguments
	 * @see {@link #startApp(MIDlet, String, String, String, int, String)}
	 * @param midlet Current MIDlet instance
	 * @param name MIDlet-Name
	 * @param vendor MIDlet-Vendor
	 * @param pushPort Push port
	 * @param cmd Command
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 * @throws IOException IO error occured while sending push data
	 */
	public static boolean startApp(MIDlet midlet, String name, String vendor, int pushPort, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, IOException
	{
		return _launchApp(midlet, name, vendor, null, pushPort, cmd);
	}
	
	/**
	 * Launches a MIDlet by Name/Vendor, UID or Push port with arguments
	 * @param midlet Current MIDlet instance
	 * @param name MIDlet-Name
	 * @param vendor MIDlet-Vendor
	 * @param pushPort Push port
	 * @param uid App's "Nokia-MIDlet-UID-1" value
	 * @param cmd Command
	 * @return true if the MIDlet suite MUST exit
	 * @throws MIDletNotFoundException if MIDlet was not found
	 * @throws ProtocolNotSupportedException if MIDlet launch protocol not supported
	 * @throws IOException IO error occured while sending push data
	 */
	public static boolean startApp(MIDlet midlet, String name, String vendor, String uid, int pushPort, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, IOException
	{
		return _launchApp(midlet, name, vendor, uid, pushPort, cmd);
	}
	
	private static boolean _launchApp(MIDlet midlet, String name, String vendor, String uid, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException
	{
		try {
			return _launchApp(midlet, name, vendor, uid, 0, cmd);
		} catch (MIDletNotFoundException e) {
			throw e;
		} catch (ProtocolNotSupportedException e) {
			throw e;
		} catch (ConnectionNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw new ConnectionNotFoundException(e.toString());
		}
	}
	
	private static boolean _launchApp(MIDlet midlet, String name, String vendor, String uid, int pushPort, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException, IOException
	{
		if(midlet == null || ((name == null || vendor == null) && uid == null)) {
			throw new IllegalArgumentException("startApp");
		}
		String from = midlet.getAppProperty("MIDletIntegration-ID");
		if(from == null) {
			midlet.getAppProperty("MIDlet-Name");
		}
		try {
			if(s40) {
				if(name == null || vendor == null) {
					throw new IllegalArgumentException("name and vendor parameters are required");
				}
				String cmd2 = (cmd != null && cmd.length() > 0 ? cmd : "empty=1");
				// s40v3-v6 method, doesn't work on ashas with xpress browser
				// location.href="localapp://jam/launch?midlet-vendor=<vendor>;midlet-nam=<name>;launchcmd=<cmd>;launchfrom=<from>"
				midlet.platformRequest(S40_LOCALAPP_URL +
						"name=" + Util.encodeURL(name) +
						"&vendor=" + Util.encodeURL(vendor) +
						"&cmd=" + Util.encodeURL(cmd2) +
						"&from=" + Util.encodeURL(from)
						);
				// TODO somehow determine when midlet is not found
				// force midlet exit because s40 wants so
				midlet.notifyDestroyed();
				return true;
			}
			if(supportsJavaApp || pushPort == 0) {
				String cmd2 = (cmd != null && cmd.length() > 0 ? Util.encodeURL(cmd) : "empty=1");
				if(uid != null && supportsJavaApp) {
					return _javaAppRequest(midlet, "midlet-uid=" + Util.encodeURL(uid) + ";" + cmd2);
				}
				return _javaAppRequest(midlet, "midlet-name=" + Util.encodeURL(name) + ";midlet-vendor=" + Util.encodeURL(vendor) + ";" + cmd2);
			}
		} catch(MIDletNotFoundException e) {
			throw e;
		} catch (IOException e) {
			if(s40 || pushPort == 0) throw e;
		}
		return _push(midlet, pushPort, cmd);
	}
	
	private static boolean _javaAppRequest(MIDlet midlet, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, ConnectionNotFoundException
	{
		try {
			return midlet.platformRequest(JAVAAPP_PROTOCOL + cmd);
		} catch (ConnectionNotFoundException e) {
			if(e.getMessage() != null) {
				if(e.getMessage().startsWith("Cannot start Java application") ||
						e.getMessage().indexOf("following error: -12") != -1) {
					throw new MIDletNotFoundException(e.getMessage());
				} else if(e.getMessage().indexOf("Invalid localapp URL") != -1 ||
						e.getMessage().indexOf("Invalid URL") != -1) {
					throw new ProtocolNotSupportedException(e.getMessage());
				}
			}
			throw e;
		}
	}
	
	private static boolean _push(MIDlet midlet, int pushPort, String cmd)
			throws MIDletNotFoundException, ProtocolNotSupportedException, IOException
	{

		if(dataConnection != null) {
			throw new IOException("busy");
		}
		exception = null;
		try {
			if(cmd == null) {
				cmd = "empty=1";
			}
			Object lock = new Object();
			Thread thread = new Thread(new MIDletIntegration(pushPort, cmd, lock));
			thread.start();
			synchronized(lock) {
				lock.wait(4000);
			}
			if(dataConnection != null) {
				thread.interrupt();
				try {
					dataConnection.close();
				} catch (Exception e) {
				}
				throw new MIDletNotFoundException();
			}
			if(exception != null) {
				IOException e = (IOException) exception;
				exception = null;
				throw e;
			}
			// no need exit on S60
			return !s60;
		} catch(InterruptedException e) {
			throw new RuntimeException(e.toString());
		} catch (Error e) {
			throw new ProtocolNotSupportedException(e.toString());
		}
	}
	
	public void run() {
		try {
			dataConnection = (DatagramConnection) Connector.open("datagram://127.0.0.1:" + pushPort);
			Datagram data = dataConnection.newDatagram(dataConnection.getMaximumLength());
			data.reset();
			data.writeUTF(cmd);
			dataConnection.send(data);
			if(s60) {
				// s60v3 bug workaround
				try {
					dataConnection.send(data);
				} catch (Exception e) {
				}
			}
			dataConnection.close();
		} catch (IOException e) {
			exception = e;
		} catch (Exception e) {
		}
        dataConnection = null;
        synchronized(lock) {
        	lock.notify();
        }
	}
	
	public static void registerPush(MIDlet midlet, int port) throws ClassNotFoundException, IOException {
		if(PushRegistry.listConnections(false).length == 0) {
			PushRegistry.registerConnection("datagram://:" + port, midlet.getClass().getName(), "*");
		}
	}
	
	public static void unregisterPush(int port) {
		try {
			if(PushRegistry.listConnections(false).length > 0) {
				PushRegistry.unregisterConnection("datagram://:" + port);
			}
		} catch (Throwable e) {
		}
	}
	
}
