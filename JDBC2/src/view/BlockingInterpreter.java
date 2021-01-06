package view;

import java.util.List;
import java.util.Scanner;

import controller.Controller;
import model.Instrument;
/* This class is taken from Leif Lindbäck.
 * Author: Leif Lindbäck
 * Title of program: jdbc-bank
 * Availability: https://github.com/KTH-IV1351/jdbc-bank/blob/master/src/main/java/se/kth/iv1351/bankjdbc/view/BlockingInterpreter.java
 */
public class BlockingInterpreter {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private Controller ctrl;
    private boolean keepReceivingCmds = false;

    /**
     * Creates a new instance that will use the specified controller for all operations.
     * 
     * @param ctrl The controller used by this instance.
     */
    public BlockingInterpreter(Controller ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * Stops the commend interpreter.
     */
    public void stop() {
        keepReceivingCmds = false;
    }

    /**
     * Interprets and performs user commands. This method will not return until the
     * UI has been stopped. The UI is stopped either when the user gives the
     * "quit" command, or when the method <code>stop()</code> is called.
     */
    public void handleCmds() {
        keepReceivingCmds = true;
        while (keepReceivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                
                    case HELP:
                        for (Command command : Command.values()) {
                            if (command == Command.ILLEGAL_COMMAND) {
                                continue;
                            }
                            System.out.println(command.toString().toLowerCase());
                        }
                        break;
                        
                    case QUIT:
                        keepReceivingCmds = false;
                        break;
                        
                    case NEW:
                    	if (!ctrl.isNullOrEmpty(cmdLine.getParameter(0))) {
                    		
                    		if(!ctrl.existAccount(cmdLine.getParameter(0)))
                    			ctrl.createAccount(cmdLine.getParameter(0));
                    		else
                    			System.out.println(" The account was created. ");
                        }
                    	else
                    		System.out.println(" Please enter your personnumber. ");
                        break;
              
                    case LIST:
                        List<Instrument> instruments = null;
              
                        if(ctrl.isNullOrEmpty(cmdLine.getParameter(0))) {
                        	System.out.println("Please enter a name of instrument");}
                        else {
                        	instruments = ctrl.getAllAvailableInstrument(cmdLine.getParameter(0));
                            	if(instruments.isEmpty()) {
                            		System.out.println("There are no available "+ cmdLine.getParameter(0));
                            		}
                            	else {
                            		for (Instrument ins : instruments) {
                            			System.out.println("id: " + ins.getID() +", "+
                                		           "brand: " + ins.getBrand() + ", "
                                                   + "kind: " + ins.getKind() + ", "
                                                   + "price: " + ins.getPrice());
                            			}
                            		}
                           	 }
                        break;
                        
                    case RENT:
                    	 if(ctrl.isNullOrEmpty(cmdLine.getParameter(0))) {
                         	System.out.println(" Please enter your personnr ");}
                    	 else if (ctrl.isNullOrEmpty(cmdLine.getParameter(1))) {
                    		 System.out.println(" Please enter the ID number of the instrument. ");
                    	 }
                    	 else if(!ctrl.existAccount(cmdLine.getParameter(0))) {
                    		 System.out.println(" Please enroll the school ( create a account)before rent a instrument. ");
                    	 }
                    	 else if(!ctrl.existInstrument(cmdLine.getParameter(1))){
                    		 System.out.println(" The instrument does not exist in the stock. "); 
                    	 }
                    	 else if(ctrl.rentedInstrument(cmdLine.getParameter(1))) {
                    		 System.out.println(" The instrument "+ cmdLine.getParameter(1)+ " Was rented. ");
                    	 }
                    	 else if(ctrl.amountOngoingRental(cmdLine.getParameter(0))>=2) {  // obs check look up if the account exist in dao
                    		 System.out.println(" The rent limit has exceeded . "); 
                    	 }
                    	 else {
                    		 ctrl.rentInstrument(cmdLine.getParameter(0), cmdLine.getParameter(1)); 
                        }
                        break;
                        
                    case LIST_RENTAL:
                   
                        if(ctrl.isNullOrEmpty(cmdLine.getParameter(0))) {
                        	System.out.println("Please enter your personnumber");
                        }
                        else if(!ctrl.existAccount(cmdLine.getParameter(0))) {
                   		 System.out.println(" Please enroll the school ( create a account)before rent a instrument. ");
                   	    }
                        else if(ctrl.amountOngoingRental(cmdLine.getParameter(0))==0) {
                   		 System.out.println(" There are no ongoing rental. ");
                   	 	}
                        else{
                        	 System.out.println(ctrl.listUntermiantedRental(cmdLine.getParameter(0)));
                        }         
                        break;
                        
                    case TERMINATE:
	                   	 if(ctrl.isNullOrEmpty(cmdLine.getParameter(0))) {
	                      	System.out.println(" Please enter your personnr ");}
	                 	 else if (ctrl.isNullOrEmpty(cmdLine.getParameter(1))) {
	                 		 System.out.println(" Please enter the ID number of the instrument. ");
	                 	 }
	                 	 else if(!ctrl.existAccount(cmdLine.getParameter(0))) {
                    		 System.out.println(" Please enroll the school ( create a account)before rent a instrument. ");
                    	 }
                    	 else if(!ctrl.existInstrument(cmdLine.getParameter(1))){
                    		 System.out.println(" The instrument does not exist in the stock. "); 
                    	 }
                    	 else if(ctrl.findTheUnterminatedRental(cmdLine.getParameter(0), cmdLine.getParameter(1))==null) {
                    		 System.out.println(" You do not have the rental. ");
                    	 }
                    	 else {
                    
                    		 ctrl.terminateRental(cmdLine.getParameter(0), cmdLine.getParameter(1));
                    	 }
                    	 
                    
                       
                       break;
                    default:
                        System.out.println("illegal command");
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String readNextLine() {
        System.out.print(PROMPT);
        return console.nextLine();
    }
    
}