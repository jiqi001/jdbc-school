package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import integration.AccountDAO;
import integration.InstrumentDBException;
import integration.InstrumentStockDAO;
import integration.RegisterAccountDBException;
import model.Account;
import model.Instrument;
import model.InstrumentException;
import model.Rental;
import model.RentalException;
import model.AccountException;


public class Controller {
	private final InstrumentStockDAO instrumentStock;
	private final AccountDAO accountDAO;

	
	public Controller() throws InstrumentDBException, RegisterAccountDBException  {
		instrumentStock = new InstrumentStockDAO();	
		accountDAO = new AccountDAO();

		
	}
	
	public List <Instrument> getAllAvailableInstrument(String instrumentKind) throws InstrumentException{
		try {
			return instrumentStock.FindAllAvailableInstrument(instrumentKind);
		} catch(Exception ex) {
			throw new InstrumentException("Unable to list available instruments",ex);
		}
		
	}

	public String listUntermiantedRental(String personnr) throws RentalException{
		try {
			StringBuilder st = new StringBuilder();
			
			Account account = getAccount(personnr);
			if(amountOngoingRental(personnr)==0) {
				throw new RentalException(" There are no ongoing rentals for you ");
				}
			
			else{
				List<Rental> rentals = instrumentStock.findUnterminatedRental(account.getStudentPK());
				
				for (Rental rental : rentals) {
					String instrumentID =rental.getInstrumentID();
					Instrument in = instrumentStock.getInstrumentInfo(instrumentID);
					st.append("InstrumentID: "+ instrumentID );
					st.append(", Instrument: " + in.getKind());
					st.append(", Price: " + in.getPrice());
					st.append(", Brand: " + in.getBrand());
					st.append(", RentDate: "+ rental.getRentDate());
					st.append("\n");
                }
			}
			return st.toString();
		} catch(Exception ex) {
			throw new RentalException("Unable to list available instruments",ex);
		}
	}
	
	
	
	// Check if the account exist
	public boolean existAccount(String personnr) throws AccountException {
		try {
			if(accountDAO.getStudentInfo(personnr)!=null){
				return true;
				}
			else return false;
			}
		catch(Exception ex){
			throw new AccountException(" Could not check if the account exist ",ex);
		}
		
	}
	
	// Create a account
	public void createAccount(String personnr) throws AccountException {
		String msg= " could not create account for "+ personnr;
		
		try {
			accountDAO.createAccount(new Account(personnr));
		}catch(Exception ex) {
			throw new AccountException(msg, ex);
		}	
	}
	//check if a instrument exist in the instrument stock.
	public boolean existInstrument(String instrumentID) throws InstrumentException {
		try {
			if(instrumentStock.getInstrumentInfo(instrumentID)!=null)
				return true;
		} 
		catch(Exception ex){
			throw new InstrumentException(" Could not check it the instrument exist ",ex);
		}
		return false;
	}
	
	// check if the instrument was already rented
	public boolean rentedInstrument(String instrumentID) throws InstrumentException {
		try {
			if(instrumentStock.alreadyRent(instrumentID))
				return true;
		}
		catch(Exception ex){
			throw new InstrumentException(" Could not check it the instrument was rented ",ex);
		}
		return false;
	}
	
	//rent a instrument
	public void rentInstrument(String personnr, String instrumentID) throws RentalException, 
		RegisterAccountDBException, InstrumentDBException, AccountException {
		
		String msg= "could not create rental for "+ personnr;
		if(personnr == null || instrumentID == null) {
			throw new RentalException(msg);
		}
		
		Account account = getAccount(personnr);
		if(account!=null) { // check if the student have created a account.
			if(instrumentStock.amountUnterminatedRental(account.getStudentPK())<2) {// if the student have not exceeded the rent limit.
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  
				LocalDateTime now = LocalDateTime.now();
				String rentDate = dtf.format(now);
				try {
					instrumentStock.createRental(new Rental(account.getStudentPK(),instrumentID,rentDate));
					} catch(Exception ex) {
						throw new RentalException(msg, ex);
						}
				}
			}
		}
	// get an Account of person number
	private Account getAccount(String personnr) throws AccountException {
		try {
			return accountDAO.getStudentInfo(personnr);
		}
		catch(Exception ex){
			throw new AccountException(" Could not check if the account exist. ", ex);
		}
		
	}
	
	//
	public int amountOngoingRental(String personnr) throws AccountException, RentalException {
		
		Account account = getAccount(personnr);
		if(account == null) {
			return 0;
		}
		else {
		try {
			return instrumentStock.amountUnterminatedRental(account.getStudentPK());
		} 
		catch(Exception ex) {
			throw new RentalException(" Could not check amount ongoing rental. ",ex);
		}
	}
	}
	// Find the unterminated rental. @param personnr is personnumber, @param 
	//instrumentId is instrument's id number.
	public Rental findTheUnterminatedRental(String personnr, String instrumentID) throws RentalException, InstrumentException, AccountException {
		Account account = getAccount(personnr);
		if(account!=null && rentedInstrument(instrumentID) ) {// if there is a student account, and the instrument was rented(instrumen exist)
			try {
				String rentDate = null;
				List<Rental> unterminatedRentals = instrumentStock.findUnterminatedRental(account.getStudentPK());
				if(unterminatedRentals != null) { // if there exist some unterminated instrument
					for(Rental rental :unterminatedRentals ) {
						if(rental.getInstrumentID().equals(instrumentID)) {// if the rental exist.
							rentDate = rental.getRentDate();
							DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  
							LocalDateTime now = LocalDateTime.now();
							String returnDate = dtf.format(now);
							return new Rental(account.getStudentPK(), 
											  instrumentID,
											  rentDate,
											  returnDate);
							}
						}
				}
			}
			catch(Exception ex){
				throw new RentalException(" Unable to terminate the rental ",ex);
				}
			}
		return null;

	}
	
	public void terminateRental(String personnr, String instrumentID) throws RegisterAccountDBException, AccountException, InstrumentException, RentalException {
	
			try {
				Rental rental =findTheUnterminatedRental(personnr,instrumentID);
				if(rental !=null) {
					instrumentStock.terminateRental(rental);	
				}
				}

			catch(Exception ex){
				throw new RentalException(" Unable to terminate the rentall ",ex);
				}
			
	}

	
	public boolean isNullOrEmpty(String str) {
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }
	//TEST
	/*
	public static void main(String[] args) throws InstrumentDBException, RegisterAccountDBException,RentalException, InstrumentException, AccountException {
		Controller c = new Controller();
		Rental rental = c.findTheUnterminatedRental("20000101","27");
		System.out.println(rental.getInstrumentID());
		c.terminateRental("20000101", "27");
	}*/
	
}



