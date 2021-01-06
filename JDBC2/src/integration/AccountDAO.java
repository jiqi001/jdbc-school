package integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.Account;

public class AccountDAO {
	
	private Connection connection;
	private PreparedStatement createAccountStmt;
	private PreparedStatement findAccountStmt;
	
	/*/
	 * Constructs a new DAO object connected to the instrumentStock database
	 */
	public AccountDAO()throws RegisterAccountDBException {
		try {
			connectToRegisterAccountDB();
			 prepareStatements();
		} catch(ClassNotFoundException| SQLException ex){
			throw new RegisterAccountDBException ("could not connect to database", ex);
		}
	}
	

	private void connectToRegisterAccountDB() throws ClassNotFoundException,SQLException  {
		connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/customer","root","root1234");
		connection.setAutoCommit(false);
	}
	
	private void prepareStatements(
			)throws SQLException {
		createAccountStmt = connection.prepareStatement("INSERT INTO student ( personnumber ) " + 
			" VALUES (?) ");
		
		findAccountStmt=connection.prepareStatement("select * from student where personnumber = ? ");
				
	}
	/*
	 * create an account.
	 */
	public void createAccount(Account account) 
			throws SQLException, RegisterAccountDBException {
		
		String msg ="Could not create account";
		int updateRows=0;
		
		try {
			//if(!accountExist(account.getPersonnr())) {
			if(getStudentInfo(account.getPersonnr())==null) {
			String personnr = account.getPersonnr();
			createAccountStmt.setString(1, personnr);
		    updateRows = createAccountStmt.executeUpdate();
		    if(updateRows!=1) {
		    	handleException(msg, null);
		    }
			connection.commit();
		    }
		}catch (SQLException ex) {
			handleException(msg , ex);	
		}
	}
	
	/*
	 * @return an Account 
	 * @param personnr is person number of a person.
	 * @throw RegisterAccountDBException if it failed to get the account.
	 */
	public Account getStudentInfo (String personnr) throws RegisterAccountDBException {
		String msg ="could not get StudentPK from the database ";
		ResultSet result = null;
		try {
			//if(accountExist(personnr)) {
			findAccountStmt.setString(1,personnr);
			result = findAccountStmt.executeQuery();
			if(result.next()) {
				return new Account(personnr, result.getString("ID"));
			}
			connection.commit();
			//}
			}catch (SQLException ex) {
				handleException(msg,ex);
		} finally {
			closeResultSet(msg, result);
			}
		return null;
	}
		
	
	private void handleException(String msg, Exception cause ) throws RegisterAccountDBException {
		String completeFail= msg;
		try {
			connection.rollback();
		}catch(SQLException rollbackExc) {
			completeFail= completeFail + "Also failed to rollback transaction because of"+ rollbackExc.getMessage();
		}
		if(cause != null) {
			throw new RegisterAccountDBException(completeFail, cause);
		}
		else {
			throw new RegisterAccountDBException(completeFail);
		}
	}
	
	private void closeResultSet(String msg, ResultSet result) throws RegisterAccountDBException {
		try {
			result.close();
		} catch(Exception ex) {
			throw new RegisterAccountDBException(msg+" Could not close result set", ex);
		}
	}
	//TEST
	/*
	public static void main(String[] args) throws RegisterAccountDBException, SQLException {
	String x ="        ";
	if(x != null && !x.trim().isEmpty()) {
	Account account = new Account(x);
	new AccountDAO().createAccount(account);
		
		System.out.println(new AccountDAO().getStudentInfo(x).getStudentPK());
		System.out.println(new AccountDAO().getStudentInfo(x).getPersonnr());
		
	}
	System.out.println("n");
	}
*/
}
