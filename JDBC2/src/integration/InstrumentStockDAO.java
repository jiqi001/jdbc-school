package integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import model.Instrument;
import model.Rental;

public class InstrumentStockDAO {
	private static final String TableName ="instrument_stock";
	private static final String Instrument_PK="id";
	private static final String InstrumentType= "instrumenttype";
	private static final String Brand ="brand";
	private static final String LeasePrice="leasePrice";
	private final static String TableName2="instrumentRental";
	
	private Connection connection;
	private PreparedStatement findAllAvailableInstrumentStmt;
	private PreparedStatement setInstrumentStateStmt;
	private PreparedStatement getInstrumentInfoStmt;
	

	private PreparedStatement createRentalStmt;
	private PreparedStatement findUnterminatedRentalStmt;
	private PreparedStatement setReturnDateStmt;
	private PreparedStatement rentedInstrumentStmt;
	
	/*/
	 * Constructs a new DAO object connected to the instrumentStock database
	 */
	public InstrumentStockDAO()throws InstrumentDBException {
		try {
			 connectToInstrumentStock();
			 prepareStatements();
		} catch(ClassNotFoundException| SQLException ex){
			throw new InstrumentDBException("could not connect to database", ex);
		}
	}
	
	// Create connection to the database.
	private void connectToInstrumentStock() throws ClassNotFoundException,SQLException  {
		connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/school","root","root1234");
		connection.setAutoCommit(false);
	}
	
	private void prepareStatements(
			)throws SQLException {
		
		findAllAvailableInstrumentStmt = connection.prepareStatement(
				" select ID, instrumentType, brand,leaseprice from " + TableName+
				"  where status = 'available' AND instrumentType = ? " +" order by leaseprice ");
		
		setInstrumentStateStmt = connection.prepareStatement(
				" UPDATE " +TableName + " SET "+ " status = ? "+" WHERE ID = ?  ");
		
		
		getInstrumentInfoStmt = connection.prepareStatement(" select * from " + TableName+
				" where ID = ? ");
		
		createRentalStmt = connection.prepareStatement(
				" INSERT INTO "+ TableName2+" (instrument_id, student_id, rentDate ) " + 
				"  VALUES ( ?, ?, ? )");
		
		rentedInstrumentStmt = connection.prepareStatement("select * from "
				+TableName2 +" where instrument_id = ? AND returnDate IS null ");
		
		findUnterminatedRentalStmt = connection.prepareStatement("select * from "
				+TableName2 +" where student_id = ? AND returnDate IS null ");
		
		setReturnDateStmt=connection.prepareStatement(" UPDATE  "
				+TableName2 +" SET returnDate = ? WHERE student_id = ? "
						+ " AND instrument_id = ? AND rentDate = ? ");
	} ;
	/*@return A list with all available instruments
	 *@param instrumentKind, The name of instrument.
	 *@throw InstrumentDBException if it failed to search availableInstruments 
	 */ 
	
	public List<Instrument> FindAllAvailableInstrument(String instrumentKind) 
			throws InstrumentDBException,SQLException {
		
		String msg ="Could not list instruments";
		ResultSet result=null;
		List<Instrument> availableInstruments = new ArrayList<>();
		try {
			findAllAvailableInstrumentStmt.setString(1,instrumentKind);
		    result = findAllAvailableInstrumentStmt.executeQuery();
			while(result.next()) {
				availableInstruments.add(new Instrument(result.getInt(Instrument_PK),
														result.getString(InstrumentType),
														result.getString(Brand),
														result.getString(LeasePrice)));
			}
			connection.commit();
		}catch (SQLException ex) {
			handleException(msg,ex);
		}  
		return availableInstruments;
	}
	/* Set instrument state to "rented" or"available" in instrument stock.
	 * @param instrumentID is id of an instrument.
	 * @param state is state of an instrument, rented or available.
	 * @throw InstrumentDBException if it failed to set state.
	 */ 
	
	private void setInstrumentState (String instrumentID,String state) throws InstrumentDBException {
		String msg =" could not update the instrumentstate ";
		try {
			setInstrumentStateStmt.setString(1,state);
			setInstrumentStateStmt.setString(2, instrumentID);
			int updateRows = setInstrumentStateStmt.executeUpdate();
			if(updateRows != 1) {
				handleException(msg, null);
			}
			connection.commit();
		} catch(SQLException ex) {
			handleException(msg, ex);
		}
	}
	
	
	/* @return Instrument Object that describe an instrument.
	 * @ instrumentID is instrument id number
	 * @ throw instrumentDBException if it failed to get the instrument from database.
	 */
	public Instrument getInstrumentInfo(String instrumentID) throws InstrumentDBException {
		
		String msg =" Could search for the instrument ";
		ResultSet result =null;
		try {
			//if(existInstrument(instrumentID)) {
			getInstrumentInfoStmt.setString(1,instrumentID);
			result = getInstrumentInfoStmt.executeQuery();
			if(result.next()) {
				return new Instrument(result.getInt(Instrument_PK),
						result.getString(InstrumentType),
						result.getString(Brand),
						result.getString(LeasePrice));
				}
			//}
			connection.commit();
			} catch(SQLException ex){
				handleException(msg,ex);
				}finally {
					closeResultSet(msg, result);
				}
		return null;
	}
	

/*@return a list of unterminated Rental.
 * @param personPK is serial number of a person, it is primary key in the person table.
 * @throw instrumentDBException if it failed to search the rental in the database.
 */
public List<Rental> findUnterminatedRental(String personPK) throws InstrumentDBException{
	String msg = " Could not search for the rental. ";
	ResultSet result = null;
	List<Rental> rentals = new ArrayList<>();
	try {
		if(amountUnterminatedRental(personPK)!=0) {
		findUnterminatedRentalStmt.setString(1, personPK);
		result = findUnterminatedRentalStmt.executeQuery();
		while(result.next()) {
			rentals.add(new Rental( result.getString("student_id"),
								    result.getString("instrument_id"),
								    result.getTimestamp(3).toString()
								   // result.getTimestamp(4).toString()
								    ));
			}
		connection.commit();
		}
		} catch(SQLException ex) {
			handleException(msg,ex);
			} finally {
			closeResultSet(msg, result);
			}
	return rentals;
}

/*	Create a rental
 * 
 */
public void createRental(Rental rental) throws InstrumentDBException{
		
		String msg ="Could not cerate instrument rental";
		int updateRows =0;
		try {
			if(!alreadyRent(rental.getInstrumentID())) {
			createRentalStmt.setString(1,rental.getInstrumentID());
			createRentalStmt.setString(2,rental.getStudentID());
			createRentalStmt.setString(3, rental.getRentDate());
			updateRows = createRentalStmt.executeUpdate();
			
			if(updateRows!=1) {
				handleException(msg, null);
			}
			setInstrumentState(rental.getInstrumentID(),"rented");
			
			connection.commit();
		
			}
	    } catch (SQLException ex) {
	    	handleException(msg, ex);
	}
		}
/*
 * terminate a @param rental
 */
public void terminateRental(Rental rental) throws InstrumentDBException {
	String msg =" Could not terminate the rental. ";
	
	try {
		setReturnDateStmt.setString(1, rental.getReturnDate());
		setReturnDateStmt.setString(2, rental.getStudentID());
		setReturnDateStmt.setString(3, rental.getInstrumentID());
		setReturnDateStmt.setString(4, rental.getRentDate());
		int updatedRows = setReturnDateStmt.executeUpdate();
		if(updatedRows !=1) {
			handleException(msg, null);
		}
		setInstrumentState(rental.getInstrumentID(),"available");
		connection.commit();	
		} catch (SQLException ex) {
			handleException(msg, ex);
		}
	
}
	/*
	 * @return boolean tells if the @param instrumentID was already rented.
	 */
	public boolean alreadyRent(String instrumentID) throws InstrumentDBException{
		String msg =" Could not check if the instrument was already rented ";
		ResultSet result =null;
		try {
			//if(existInstrument(instrumentID)) {
			if(getInstrumentInfo(instrumentID)!=null) {
			rentedInstrumentStmt.setString(1,instrumentID);
			result = rentedInstrumentStmt.executeQuery();
			if(result.next()) {
				return true;
			}
				connection.commit();
			}
		} catch(SQLException ex){
			handleException(msg,ex);
		}finally {
			closeResultSet(msg, result);
			}
		return false;
	}
	
		
/*@return integer of amount unterminated rental
 * @personPK is primary key of a person in the person table.
 * */
	public int amountUnterminatedRental(String personPK) throws InstrumentDBException {
		String msg =" Could not search if there are any unterminated rentals  ";
		int amountUnerminatedRental = 0;
		ResultSet result =null;
		try {
			findUnterminatedRentalStmt.setString(1,personPK);
		    result = findUnterminatedRentalStmt.executeQuery();
			while(result.next()) {
				++ amountUnerminatedRental;
			}
			connection.commit();
		} catch(SQLException ex) {
			handleException(msg,ex);
		}finally {
			closeResultSet(msg, result);
			}
		return amountUnerminatedRental;
		
	}
	
	

	private void handleException(String msg, Exception cause) throws InstrumentDBException{
		String completeFail =msg;
		try {
			connection.rollback();
		} catch(SQLException rollbackExc) {
			completeFail = msg + " Also failed to rollback because of"+ rollbackExc.getMessage();
		}
		if(cause != null) {
			throw new InstrumentDBException(completeFail, cause);
		}
		else {
			  throw new InstrumentDBException(completeFail);
			  }
	}
		
	
	private void closeResultSet(String msg, ResultSet result) throws InstrumentDBException {
		try {
			result.close();
		} catch(Exception ex) {
			
			throw new InstrumentDBException(msg +"Could not close result set", ex);
		}
	}
	

	/* 
	//TEST
	public static void main(String[] args) throws InstrumentDBException, SQLException {
		try {
		InstrumentStockDAO instru = new InstrumentStockDAO();
		
		System.out.println(instru.amountUnterminatedRental("2"));
		instru.setInstrumentRented("25");
		//instru.setInstrumentRented("13");
		String g="guitar";
		List<Rental> rentals =new ArrayList();
				rentals=instru.findUnterminatedRental("2");
	
		   for (Rental x : rentals) {
               System.out.println("instrumentID: " + x.getInstrumentID() + ", "
                                + "rentDAte: " + x.getRentDate() + ", "
                                + "studentID: " + x.getStudentID());
           }
		
		
		}
		finally {
			
		}
	}*/
	
}


