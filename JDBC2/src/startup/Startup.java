package startup;

import controller.Controller;
import integration.InstrumentDBException;
import integration.RegisterAccountDBException;
import view.BlockingInterpreter;

public class Startup {

	public static void main(String[] args) throws RegisterAccountDBException {
		try {
			Controller contr= new Controller();
			new BlockingInterpreter(contr).handleCmds();
		}
		catch(InstrumentDBException ex) {
			System.out.println("Could not connect to datasource");
			ex.printStackTrace();
		}

	}

}
