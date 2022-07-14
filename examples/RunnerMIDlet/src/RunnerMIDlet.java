import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;

import midletintegration.Util;
import midletintegration.MIDletIntegration;
import midletintegration.MIDletNotFoundException;
import midletintegration.ProtocolNotSupportedException;

public class RunnerMIDlet extends MIDlet implements CommandListener {

	private final Command runCmd = new Command("Run", Command.OK, 1);
	private Form form = new Form("Runnner Example");
	private TextField urlField;

	protected void destroyApp(boolean b) {
	}

	protected void pauseApp() {
	}

	protected void startApp() {
		form.addCommand(runCmd);
		form.setCommandListener(this);
		form.append(urlField = new TextField("URL", "", 200, TextField.URL));
		Display.getDisplay(this).setCurrent(form);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == runCmd) {
			try {
				if(MIDletIntegration.startApp(this, "Receiver Example", "Example", 1270, "url=" + Util.encodeURL(urlField.getString()))) {
					notifyDestroyed();
				}
			} catch (MIDletNotFoundException e) {
				alert("MIDlet not found!");
				e.printStackTrace();
			} catch (ProtocolNotSupportedException e) {
				alert("Launching protocol is not supported!");
				e.printStackTrace();
			} catch (Exception e) {
				alert("Unknown error: " + e.toString() + "\n");
				e.printStackTrace();
			}
		}
	}
	
	private void alert(String msg) {
		Alert alert = new Alert("");
		alert.setString(msg);
		Display.getDisplay(this).setCurrent(alert);
	}

}
