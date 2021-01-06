package integration;

public class RegisterAccountDBException extends Exception {

	
	public RegisterAccountDBException (String reason) {
	super(reason);
	}
	
	public RegisterAccountDBException (String reason, Throwable rootCause) {
		super(reason,rootCause);
	}

}
