package midletintegration;

import java.io.IOException;

/**
 * Thrown if application launching is not supported
 * 
 * @author Shinovon
 */
public class ProtocolNotSupportedException extends IOException {

	public ProtocolNotSupportedException() {
		super();
	}
	
	public ProtocolNotSupportedException(String s) {
		super(s);
	}

}
