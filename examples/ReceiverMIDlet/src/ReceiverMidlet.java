import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

import midletintegration.MIDletIntegration;
import midletintegration.Util;

public class ReceiverMidlet extends MIDlet implements CommandListener {
	
	private final Command updateCmd = new Command("Update", Command.OK, 1);
	private Form form = new Form("Receiver Example");

	private static boolean started;

	protected void destroyApp(boolean b) {
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		if(started) {
			check();
			return;
		}
		started = true;
		form.addCommand(updateCmd);
		form.setCommandListener(this);
		Display.getDisplay(this).setCurrent(form);
		check();
	}

	private void check() {
		form.deleteAll();
		if(MIDletIntegration.checkLaunchArguments()) {
			form.append("Launch request detected!\n");
			form.append("URL: " + Util.decodeURL(System.getProperty("url")) + "\n");
			form.append("Full command: " + MIDletIntegration.getAllLaunchArguments().toString() + "\n");
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(c == updateCmd) {
			check();
		}
	}

}
