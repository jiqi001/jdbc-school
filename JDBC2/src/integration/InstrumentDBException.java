package integration;
/*/
 * Thrown when a call to the instrument_stock database fails.
 */
public class InstrumentDBException extends Exception {
	
	public InstrumentDBException(String reason) {
	super(reason);
	}
	
	public InstrumentDBException(String reason, Throwable rootCause) {
		super(reason,rootCause);
	}

}
