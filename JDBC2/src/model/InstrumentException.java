package model;

public class InstrumentException extends Exception {
	
	public InstrumentException(String reason) {
		super(reason);
	}
	
	public InstrumentException(String reason, Throwable rootCause) {
		super(reason,rootCause);
	}
}
