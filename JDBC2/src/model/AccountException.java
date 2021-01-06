package model;

public class AccountException extends Exception {

	  public AccountException(String reason) {
	        super(reason);
	    }

	    public AccountException(String reason, Throwable rootCause) {
	        super(reason, rootCause);
	    }
}

