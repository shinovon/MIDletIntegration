import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

import midletintegration.MIDletIntegration;
import midletintegration.Util;

/**
 * Receiver MIDlet Example
 * 
 * @author Shinovon
 * @author curoviyxru (Mathew)
 * @version 1.0
 * 
 */
public class ReceiverMidlet extends MIDlet implements CommandListener {
	
	private final Command updateCmd = new Command("Update", Command.OK, 1);
	private Form form = new Form("Receiver Example");
	//private Thread thread;

	private static boolean started;

	protected void destroyApp(boolean b) {
		//thread.interrupt();
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		if(started) {
			check();
			return;
		}
		try {
			MIDletIntegration.registerPush(this, 1270);
		} catch (Exception e) {
			form.append("Push registration failed: " + e.toString() + "\n");
		}
		started = true;
		form.addCommand(updateCmd);
		form.setCommandListener(this);
		Display.getDisplay(this).setCurrent(form);
		check();
		/*
		thread = new Thread() {
			public void run() {
				try {
					while(true) {
						Thread.sleep(1000);
						check();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		*/
	}

	private void check() {
		try {
			if(MIDletIntegration.checkLaunch()) {
				form.append("Launch request detected!\n");
				String s = MIDletIntegration.getLaunchCommand();
				Hashtable args = MIDletIntegration.getArguments(s);
				form.append("URL: " + Util.decodeURL((String) args.get("url")) + "\n");
				form.append("Full command: " + s+ "\n");
				this.resumeRequest();
			}
		} catch (Exception e) {
			e.printStackTrace();
			form.append(e.toString() + "\n");
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(c == updateCmd) {
			check();
		}
	}

}
