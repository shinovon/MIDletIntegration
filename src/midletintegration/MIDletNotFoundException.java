package midletintegration;

import java.io.IOException;

/**
 * Thrown if matched application cannot be found
 * 
 * @author Shinovon
 * 
 */
public class MIDletNotFoundException extends IOException {

	public MIDletNotFoundException() {
		super();
	}

	public MIDletNotFoundException(String s) {
		super(s);
	}

}
